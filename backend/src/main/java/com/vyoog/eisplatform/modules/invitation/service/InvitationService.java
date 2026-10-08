package com.vyoog.eisplatform.modules.invitation.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.invitation.dto.InvitationDtos.*;
import com.vyoog.eisplatform.modules.invitation.model.InvitationStatus;
import com.vyoog.eisplatform.modules.invitation.model.OrganizationInvitation;
import com.vyoog.eisplatform.modules.invitation.repository.InvitationRepository;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * REQ-TEN-008 Invite user. Who may invite is {@code INVITE_USERS} (an organization administrator
 * always; another member only through an individual grant). Everything is enforced here, never only
 * in the screens. Seats are not reserved by a pending invitation (BR-INV-010).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    static final Duration LIFETIME = Duration.ofDays(7);
    public static final String PERMISSION = "INVITE_USERS";
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String NO_PERMISSION = "You do not have permission to invite users.";
    private static final String INVALID_LINK = "This invitation link is invalid or no longer available.";
    private static final String UNAVAILABLE = "This organization is currently unavailable for new members.";
    private static final String NO_SEAT = "There are no available seats in this organization.";

    private final InvitationRepository invitationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgNodeRepository nodeRepository;
    private final OrganizationMemberService memberService;
    private final MemberAccessService memberAccessService;
    private final AuthorizationService authorizationService;
    private final InvitationAuditor auditor;
    private final EmailService emailService;
    private final KeycloakAdminClient keycloakAdminClient;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ------------------------------------------------------------ inviter side

    public CreateResult create(Long customerId, CreateInvitationRequest request) {
        OrganizationMember inviter = requireInviter(customerId);
        Long orgId = inviter.getOrganizationId();
        Organization org = requireEligibleOrganization(orgId);
        String email = normalise(request.email());
        OrgRole role = parseRole(request.orgRole());
        requireMayAssign(inviter, role);
        requireInvitable(orgId, email);
        Long nodeId = validNode(orgId, request.orgNodeId());

        invitationRepository.findByOrganizationIdAndNormalizedEmailAndStatus(orgId, email, InvitationStatus.PENDING).ifPresent(existing -> {
            if (existing.getExpiresAt().isAfter(Instant.now())) {
                throw new DuplicateResourceException("An invitation to this email is already pending. Resend it instead.");
            }
            markExpired(existing);
        });
        requireCapacity(orgId);

        String token = newToken();
        OrganizationInvitation inv = new OrganizationInvitation();
        inv.setOrganizationId(orgId);
        inv.setEmail(email);
        inv.setNormalizedEmail(email);
        inv.setOrgRole(role);
        inv.setOrgNodeId(nodeId);
        inv.setTokenHash(hash(token));
        inv.setInvitedByCustomerId(customerId);
        inv.setExpiresAt(Instant.now().plus(LIFETIME));
        inv = invitationRepository.save(inv);
        auditor.success("INVITATION_CREATED", customerId, null, inv.getId(), orgId, "Invited " + email + " as " + role);
        return new CreateResult(toDto(inv), sendEmail(inv, org, token));
    }

    public List<InvitationDto> list(Long customerId, String status) {
        OrganizationMember inviter = requireInviter(customerId);
        List<OrganizationInvitation> all = isAdmin(inviter)
            ? invitationRepository.findByOrganizationIdOrderByCreatedAtDesc(inviter.getOrganizationId())
            : invitationRepository.findByOrganizationIdAndInvitedByCustomerIdOrderByCreatedAtDesc(inviter.getOrganizationId(), customerId);
        return dtos(all.stream().peek(this::expireIfDue)
            .filter(i -> status == null || status.isBlank() || i.getStatus().name().equalsIgnoreCase(status)).toList());
    }

    /** Read-only list for a platform administrator (REQ-TEN-007). */
    public List<InvitationDto> adminList(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return dtos(invitationRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream().peek(this::expireIfDue).toList());
    }

    public CreateResult resend(Long customerId, Long invitationId) {
        OrganizationMember inviter = requireInviter(customerId);
        OrganizationInvitation inv = scoped(inviter, invitationId);
        if (inv.getStatus() == InvitationStatus.ACCEPTED) {
            throw new InvalidStateException("This invitation has already been accepted.");
        }
        Long orgId = inv.getOrganizationId();
        Organization org = requireEligibleOrganization(orgId);
        requireMayAssign(inviter, inv.getOrgRole());
        requireInvitable(orgId, inv.getNormalizedEmail());
        invitationRepository.findByOrganizationIdAndNormalizedEmailAndStatus(orgId, inv.getNormalizedEmail(), InvitationStatus.PENDING)
            .filter(other -> !other.getId().equals(inv.getId()) && other.getExpiresAt().isAfter(Instant.now()))
            .ifPresent(other -> {
                throw new DuplicateResourceException("An invitation to this email is already pending. Resend that one instead.");
            });
        requireCapacity(orgId);
        if (inv.getOrgNodeId() != null && validNodeOrNull(orgId, inv.getOrgNodeId()) == null) {
            inv.setOrgNodeId(null);
        }
        String token = newToken();
        Instant now = Instant.now();
        inv.setTokenHash(hash(token));
        inv.setStatus(InvitationStatus.PENDING);
        inv.setExpiresAt(now.plus(LIFETIME));
        inv.setLastSentAt(now);
        inv.setUpdatedAt(now);
        inv.setRevokedAt(null);
        inv.setDeclinedAt(null);
        inv.setSendCount(inv.getSendCount() + 1);
        invitationRepository.save(inv);
        auditor.success("INVITATION_RESENT", customerId, null, inv.getId(), orgId, "Resent invitation to " + inv.getEmail());
        return new CreateResult(toDto(inv), sendEmail(inv, org, token));
    }

    public InvitationDto revoke(Long customerId, Long invitationId) {
        OrganizationMember inviter = requireInviter(customerId);
        OrganizationInvitation inv = scoped(inviter, invitationId);
        expireIfDue(inv);
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidStateException("Only a pending invitation can be revoked.");
        }
        inv.setStatus(InvitationStatus.REVOKED);
        inv.setRevokedAt(Instant.now());
        inv.setUpdatedAt(Instant.now());
        invitationRepository.save(inv);
        auditor.success("INVITATION_REVOKED", customerId, null, inv.getId(), inv.getOrganizationId(), "Revoked invitation to " + inv.getEmail());
        return toDto(inv);
    }

    /** Active nodes of the caller's organization with their path, for the invite form (BR-INV-014). */
    @Transactional(readOnly = true)
    public List<NodeOption> structureNodes(Long customerId) {
        OrganizationMember inviter = requireInviter(customerId);
        List<OrgNode> nodes = nodeRepository.findByOrganizationIdOrderBySortOrderAscNameAsc(inviter.getOrganizationId());
        Map<Long, OrgNode> byId = nodes.stream().collect(Collectors.toMap(OrgNode::getId, n -> n));
        return nodes.stream().filter(OrgNode::isActive)
            .map(n -> new NodeOption(n.getId(), n.getName(), n.getNodeType(), pathOf(n, byId)))
            .sorted(Comparator.comparing(NodeOption::path, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    // ------------------------------------------------ INVITE_USERS delegation

    @Transactional(readOnly = true)
    public InvitersDto inviters(Long customerId) {
        OrganizationMember admin = requireAdmin(customerId);
        List<Long> ids = memberAccessService.grantedMemberIds(PERMISSION);
        return new InvitersDto(memberRepository.findAllById(ids).stream()
            .filter(m -> m.getOrganizationId().equals(admin.getOrganizationId())).map(OrganizationMember::getId).toList());
    }

    public InvitersDto setInvitePermission(Long customerId, Long memberId, boolean allowed) {
        OrganizationMember admin = requireAdmin(customerId);
        OrganizationMember target = memberRepository.findById(memberId)
            .filter(m -> m.getOrganizationId().equals(admin.getOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (target.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot change your own permissions.");
        }
        if (target.getStatus() != MembershipStatus.ACTIVE) {
            throw new InvalidStateException("Only an active member can be given this permission.");
        }
        memberAccessService.setGranted(target, PERMISSION, allowed, customerId);
        auditor.success(allowed ? "INVITE_PERMISSION_GRANTED" : "INVITE_PERMISSION_REMOVED", customerId, null, null,
            admin.getOrganizationId(), (allowed ? "Allowed" : "Removed") + " invitations for member " + target.getId());
        return inviters(customerId);
    }

    // ----------------------------------------------------------- invited side

    public Preview preview(String rawToken) {
        OrganizationInvitation inv = byToken(rawToken);
        expireIfDue(inv);
        if (inv.getStatus() != InvitationStatus.PENDING) {
            return new Preview(inv.getStatus().name(), null, null, null, null, null, null, false, false);
        }
        Organization org = organizationRepository.findById(inv.getOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(INVALID_LINK));
        return new Preview(inv.getStatus().name(), inv.getEmail(), org.getName(), nameOf(inv.getInvitedByCustomerId()),
            inv.getOrgRole().name(), nodePath(inv), inv.getExpiresAt(),
            customerRepository.findByEmailIgnoreCase(inv.getNormalizedEmail()).isPresent(), eligible(org));
    }

    public AcceptResult createAccount(String rawToken, AccountRequest request) {
        OrganizationInvitation inv = requireUsable(byToken(rawToken));
        Organization org = requireEligibleOrganization(inv.getOrganizationId());
        if (customerRepository.findByEmailIgnoreCase(inv.getNormalizedEmail()).isPresent()) {
            throw new DuplicateResourceException("An account with this email already exists. Please sign in to accept.");
        }
        String first = required(request.firstName(), "First name");
        String last = required(request.lastName(), "Last name");
        PasswordPolicy.validate(request.password(), request.confirmPassword());
        assertSeatForAcceptance(inv, null);

        Optional<String> sub = keycloakAdminClient.createUser(inv.getEmail(), first, last, request.password(), true);
        if (sub.isEmpty()) {
            throw new IllegalStateException("Could not create your account right now. Please try again shortly.");
        }
        Customer customer = new Customer();
        customer.setEmail(inv.getEmail());
        customer.setFirstName(first);
        customer.setLastName(last);
        customer.setStatus(RegistrationStatus.COMPLETED);
        customer.setKeycloakSub(sub.get());
        customer = customerRepository.save(customer);
        completeAcceptance(inv, customer);
        return new AcceptResult(true, org.getName());
    }

    public AcceptResult accept(String rawToken, Long customerId) {
        OrganizationInvitation inv = requireUsable(byToken(rawToken));
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!normalise(customer.getEmail()).equals(inv.getNormalizedEmail())) {
            throw new ForbiddenException("This invitation was sent to a different email address. Sign in with that account.");
        }
        Organization org = organizationRepository.findById(inv.getOrganizationId()).orElseThrow(() -> new ResourceNotFoundException(INVALID_LINK));
        completeAcceptance(inv, customer);
        return new AcceptResult(true, org.getName());
    }

    public void decline(String rawToken) {
        OrganizationInvitation inv = requireUsable(byToken(rawToken));
        inv.setStatus(InvitationStatus.DECLINED);
        inv.setDeclinedAt(Instant.now());
        inv.setUpdatedAt(Instant.now());
        invitationRepository.save(inv);
        auditor.success("INVITATION_DECLINED", null, inv.getEmail(), inv.getId(), inv.getOrganizationId(), "Invitation declined by " + inv.getEmail());
    }

    /** Hourly job (REQ-TEN-008.5): persists EXPIRED for overdue PENDING invitations. */
    public int expireOverdue() {
        List<OrganizationInvitation> due = invitationRepository.findByStatusAndExpiresAtBefore(InvitationStatus.PENDING, Instant.now());
        due.forEach(this::markExpired);
        return due.size();
    }

    // ------------------------------------------------------------- internals

    private void completeAcceptance(OrganizationInvitation inv, Customer customer) {
        Long orgId = inv.getOrganizationId();
        Organization org = organizationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException(INVALID_LINK));
        if (!eligible(org)) {
            auditor.failure("INVITATION_ACCEPT_FAILED_ORGANIZATION", customer.getId(), customer.getEmail(), inv.getId(), orgId, "Organization not eligible");
            throw new InvalidStateException(UNAVAILABLE);
        }
        Optional<OrganizationMember> active = memberRepository.findFirstByCustomerIdAndStatus(customer.getId(), MembershipStatus.ACTIVE);
        if (active.isPresent()) {
            if (active.get().getOrganizationId().equals(orgId)) {
                throw new DuplicateResourceException("This user is already a member of this organization.");
            }
            auditor.failure("INVITATION_ACCEPT_FAILED_OTHER_ORGANIZATION", customer.getId(), customer.getEmail(), inv.getId(), orgId,
                "Account already belongs to organization " + active.get().getOrganizationId());
            throw new InvalidStateException("This account already belongs to another organization.");
        }
        Optional<OrganizationMember> existing = memberRepository.findByOrganizationIdAndCustomerId(orgId, customer.getId());
        if (existing.isPresent() && existing.get().getStatus() == MembershipStatus.SUSPENDED) {
            throw new InvalidStateException("This user is currently suspended. Reactivate the existing membership instead.");
        }
        OrganizationMember member;
        try {
            member = existing.isPresent()
                ? memberService.readmitMember(existing.get().getId(), inv.getOrgRole())
                : memberService.addMember(orgId, customer.getId(), inv.getOrgRole());
        } catch (SeatLimitExceededException e) {
            auditor.failure("INVITATION_ACCEPT_FAILED_SEAT", customer.getId(), customer.getEmail(), inv.getId(), orgId, "No seat available");
            throw new SeatLimitExceededException(NO_SEAT);
        }
        Long nodeId = inv.getOrgNodeId() == null ? null : validNodeOrNull(orgId, inv.getOrgNodeId());
        if (nodeId != null) {
            member.setOrgNodeId(nodeId);
            memberRepository.save(member);
        }
        Instant now = Instant.now();
        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(now);
        inv.setUpdatedAt(now);
        inv.setAcceptedByCustomerId(customer.getId());
        invitationRepository.save(inv);
        auditor.success("INVITATION_ACCEPTED", customer.getId(), customer.getEmail(), inv.getId(), orgId, "Joined as " + inv.getOrgRole());
    }

    /** The seat check for a brand-new account, done before anything is created. */
    private void assertSeatForAcceptance(OrganizationInvitation inv, Customer customer) {
        try {
            memberService.assertSeatAvailable(inv.getOrganizationId());
        } catch (SeatLimitExceededException e) {
            auditor.failure("INVITATION_ACCEPT_FAILED_SEAT", customer == null ? null : customer.getId(), inv.getEmail(), inv.getId(),
                inv.getOrganizationId(), "No seat available");
            throw new SeatLimitExceededException(NO_SEAT);
        }
    }

    private OrganizationMember requireInviter(Long customerId) {
        OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new ForbiddenException(NO_PERMISSION));
        if (!isAdmin(member) && !memberAccessService.hasPermission(member, PERMISSION)) {
            throw new ForbiddenException(NO_PERMISSION);
        }
        return member;
    }

    private OrganizationMember requireAdmin(Long customerId) {
        return memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .filter(this::isAdmin).orElseThrow(() -> new ForbiddenException("You do not have permission to do this"));
    }

    private boolean isAdmin(OrganizationMember member) {
        return member.getOrgRole() == OrgRole.ORG_ADMIN;
    }

    private void requireMayAssign(OrganizationMember inviter, OrgRole role) {
        if (role == OrgRole.ORG_ADMIN && !isAdmin(inviter)) {
            throw new ForbiddenException("Only an organization administrator can invite another administrator.");
        }
    }

    private OrganizationInvitation scoped(OrganizationMember inviter, Long id) {
        return invitationRepository.findById(id)
            .filter(i -> i.getOrganizationId().equals(inviter.getOrganizationId()))
            .filter(i -> isAdmin(inviter) || i.getInvitedByCustomerId().equals(inviter.getCustomerId()))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
    }

    private boolean eligible(Organization org) {
        return authorizationService.organizationInGoodStanding(org.getStatus())
            && org.getLifecycleStatus() == OrganizationLifecycleStatus.ACTIVE;
    }

    private Organization requireEligibleOrganization(Long orgId) {
        Organization org = organizationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (!eligible(org)) {
            throw new InvalidStateException(UNAVAILABLE);
        }
        return org;
    }

    private void requireCapacity(Long orgId) {
        if (!memberService.hasSeatCapacity(orgId)) {
            throw new SeatLimitExceededException(NO_SEAT);
        }
    }

    /** BR-INV-009: an ACTIVE or SUSPENDED member of this organization cannot be invited. */
    private void requireInvitable(Long orgId, String email) {
        customerRepository.findByEmailIgnoreCase(email).flatMap(c -> memberRepository.findByOrganizationIdAndCustomerId(orgId, c.getId()))
            .ifPresent(m -> {
                if (m.getStatus() == MembershipStatus.ACTIVE) {
                    throw new DuplicateResourceException("This user is already a member of this organization.");
                }
                if (m.getStatus() == MembershipStatus.SUSPENDED) {
                    throw new InvalidStateException("This user is currently suspended. Reactivate the existing membership instead.");
                }
            });
    }

    private Long validNode(Long orgId, Long nodeId) {
        if (nodeId == null) {
            return null;
        }
        Long valid = validNodeOrNull(orgId, nodeId);
        if (valid == null) {
            throw new IllegalArgumentException("Choose an active structure node of this organization.");
        }
        return valid;
    }

    private Long validNodeOrNull(Long orgId, Long nodeId) {
        return nodeRepository.findByIdAndOrganizationId(nodeId, orgId).filter(OrgNode::isActive).map(OrgNode::getId).orElse(null);
    }

    private OrganizationInvitation byToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 200) {
            throw new ResourceNotFoundException(INVALID_LINK);
        }
        return invitationRepository.findByTokenHash(hash(rawToken.trim())).orElseThrow(() -> new ResourceNotFoundException(INVALID_LINK));
    }

    /** The link can still be used: PENDING and not past its expiry; otherwise the reason (BR-INV-006/007). */
    private OrganizationInvitation requireUsable(OrganizationInvitation inv) {
        expireIfDue(inv);
        switch (inv.getStatus()) {
            case PENDING:
                return inv;
            case EXPIRED:
                throw new InvalidStateException("This invitation has expired. Please request a new invitation.");
            case ACCEPTED:
                throw new InvalidStateException("This invitation has already been accepted.");
            default:
                throw new InvalidStateException("This invitation is no longer valid.");
        }
    }

    private void expireIfDue(OrganizationInvitation inv) {
        if (inv.getStatus() == InvitationStatus.PENDING && !inv.getExpiresAt().isAfter(Instant.now())) {
            markExpired(inv);
        }
    }

    private void markExpired(OrganizationInvitation inv) {
        if (inv.getStatus() != InvitationStatus.PENDING) {
            return;
        }
        inv.setStatus(InvitationStatus.EXPIRED);
        inv.setUpdatedAt(Instant.now());
        invitationRepository.save(inv);
        auditor.success("INVITATION_EXPIRED", null, null, inv.getId(), inv.getOrganizationId(), "Invitation to " + inv.getEmail() + " expired");
    }

    private boolean sendEmail(OrganizationInvitation inv, Organization org, String rawToken) {
        try {
            return emailService.sendInvitationEmail(inv.getEmail(), org.getName(), nameOf(inv.getInvitedByCustomerId()),
                inv.getOrgRole() == OrgRole.ORG_ADMIN ? "Organization administrator" : "Member", nodePath(inv), inv.getExpiresAt(),
                frontendUrl + "/invitations/" + rawToken);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private List<InvitationDto> dtos(List<OrganizationInvitation> list) {
        return list.stream().map(this::toDto).toList();
    }

    private InvitationDto toDto(OrganizationInvitation i) {
        return new InvitationDto(i.getId(), i.getEmail(), i.getStatus().name(), i.getOrgRole().name(), i.getOrgNodeId(), nodePath(i),
            i.getInvitedByCustomerId(), nameOf(i.getInvitedByCustomerId()), i.getCreatedAt(), i.getExpiresAt(), i.getAcceptedAt(),
            i.getDeclinedAt(), i.getRevokedAt(), i.getLastSentAt(), i.getSendCount(),
            i.getAcceptedByCustomerId() == null ? null : nameOf(i.getAcceptedByCustomerId()));
    }

    private String nodePath(OrganizationInvitation i) {
        if (i.getOrgNodeId() == null) {
            return null;
        }
        Map<Long, OrgNode> byId = nodeRepository.findByOrganizationIdOrderBySortOrderAscNameAsc(i.getOrganizationId()).stream()
            .collect(Collectors.toMap(OrgNode::getId, n -> n));
        OrgNode node = byId.get(i.getOrgNodeId());
        return node == null ? null : pathOf(node, byId);
    }

    private static String pathOf(OrgNode node, Map<Long, OrgNode> byId) {
        LinkedList<String> names = new LinkedList<>();
        for (OrgNode n = node; n != null; n = n.getParentId() == null ? null : byId.get(n.getParentId())) {
            names.addFirst(n.getName());
        }
        return String.join(" › ", names);
    }

    private String nameOf(Long customerId) {
        return customerRepository.findById(customerId)
            .map(c -> ((c.getFirstName() == null ? "" : c.getFirstName()) + " " + (c.getLastName() == null ? "" : c.getLastName())).trim())
            .filter(s -> !s.isEmpty()).orElse(null);
    }

    static String normalise(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 255 || !EMAIL.matcher(value).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return value;
    }

    private static OrgRole parseRole(String role) {
        try {
            return OrgRole.valueOf(role == null ? "" : role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Choose an organization role.");
        }
    }

    private static String required(String value, String field) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty() || v.length() > 100) {
            throw new IllegalArgumentException(field + " is required (up to 100 characters).");
        }
        return v;
    }

    static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
