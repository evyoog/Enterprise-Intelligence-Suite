package com.vyoog.eisplatform.modules.project.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.project.dto.ProjectDtos.*;
import com.vyoog.eisplatform.modules.project.model.OrganizationGroupProduct;
import com.vyoog.eisplatform.modules.project.repository.OrganizationGroupProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REQ-TEN-009 Projects: a group of an organization's members plus products (C88). The product access of
 * every member is derived from the projects they belong to and recorded in {@code organization_product_access}
 * with the project as its source (BR-PRJ-004 to 006). Everything is checked here, never only in the screens.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String MANAGE_PRODUCT_ACCESS = "MANAGE_PRODUCT_ACCESS";
    private static final String NO_PERMISSION = "You do not have permission to do this";
    private static final String NOT_FOUND = "Project not found";

    private final OrganizationGroupRepository groupRepository;
    private final OrganizationGroupMemberRepository groupMemberRepository;
    private final OrganizationGroupProductRepository groupProductRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final MemberAccessService memberAccessService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // ------------------------------------------------------------------ projects

    @Transactional(readOnly = true)
    public List<ProjectDto> list(Long customerId) {
        OrganizationMember caller = requireAny(customerId, MANAGE_USERS, MANAGE_PRODUCT_ACCESS);
        return groupRepository.findByOrganizationIdAndKind(caller.getOrganizationId(), GroupKind.PROJECT).stream()
            .sorted(Comparator.comparing(g -> g.getName().toLowerCase(Locale.ROOT)))
            .map(this::toDto).toList();
    }

    public ProjectDto create(Long customerId, ProjectRequest request) {
        OrganizationMember caller = require(customerId, MANAGE_USERS);
        String name = cleanName(request.name());
        requireUniqueName(caller.getOrganizationId(), name, null);
        OrganizationGroup project = new OrganizationGroup();
        project.setOrganizationId(caller.getOrganizationId());
        project.setKind(GroupKind.PROJECT);
        project.setName(name);
        project.setDescription(cleanDescription(request.description()));
        project = groupRepository.save(project);
        audit("PROJECT_CREATED", customerId, project, "Created project " + name);
        return toDto(project);
    }

    public ProjectDto update(Long customerId, Long projectId, ProjectRequest request) {
        OrganizationMember caller = require(customerId, MANAGE_USERS);
        OrganizationGroup project = project(caller, projectId);
        String name = cleanName(request.name());
        requireUniqueName(caller.getOrganizationId(), name, project.getId());
        project.setName(name);
        project.setDescription(cleanDescription(request.description()));
        groupRepository.save(project);
        audit("PROJECT_UPDATED", customerId, project, "Updated project " + name);
        return toDto(project);
    }

    /** BR-PRJ-010: takes back the access the project granted, then removes it. */
    public void delete(Long customerId, Long projectId) {
        OrganizationMember caller = require(customerId, MANAGE_USERS);
        OrganizationGroup project = project(caller, projectId);
        List<Long> memberIds = groupMemberRepository.findByGroupId(projectId).stream()
            .map(OrganizationGroupMember::getOrganizationMemberId).toList();
        int products = groupProductRepository.findByGroupId(projectId).size();
        groupMemberRepository.deleteByGroupId(projectId);
        groupProductRepository.deleteByGroupId(projectId);
        groupRepository.flush();
        memberIds.forEach(id -> memberRepository.findById(id).ifPresent(m -> recompute(m, customerId)));
        groupRepository.delete(project);
        audit("PROJECT_DELETED", customerId, project, "Deleted project " + project.getName() + " (" + memberIds.size()
            + " members, " + products + " products)");
    }

    // ------------------------------------------------------------------ members

    public ProjectDto addMember(Long customerId, Long projectId, Long memberId) {
        OrganizationMember caller = require(customerId, MANAGE_USERS);
        OrganizationGroup project = project(caller, projectId);
        OrganizationMember target = memberRepository.findById(memberId)
            .filter(m -> m.getOrganizationId().equals(caller.getOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (target.getStatus() != MembershipStatus.ACTIVE) {
            throw new InvalidStateException("Only an active member can be added to a project.");
        }
        if (groupMemberRepository.findByGroupIdAndOrganizationMemberId(projectId, memberId).isEmpty()) {
            OrganizationGroupMember gm = new OrganizationGroupMember();
            gm.setGroupId(projectId);
            gm.setOrganizationMemberId(memberId);
            groupMemberRepository.save(gm);
            audit("PROJECT_MEMBER_ADDED", customerId, project, "Added member " + memberId + " to project " + project.getName());
            recompute(target, customerId);
        }
        return toDto(project);
    }

    public ProjectDto removeMember(Long customerId, Long projectId, Long memberId) {
        OrganizationMember caller = require(customerId, MANAGE_USERS);
        OrganizationGroup project = project(caller, projectId);
        groupMemberRepository.findByGroupIdAndOrganizationMemberId(projectId, memberId).ifPresent(gm -> {
            groupMemberRepository.delete(gm);
            groupMemberRepository.flush();
            audit("PROJECT_MEMBER_REMOVED", customerId, project, "Removed member " + memberId + " from project " + project.getName());
            memberRepository.findById(memberId).ifPresent(m -> recompute(m, customerId));
        });
        return toDto(project);
    }

    // ------------------------------------------------------------------ products (the project's only resource, PR-1 A)

    /** Adds the product, or changes its role when it is already in the project. */
    public ProjectDto setProduct(Long customerId, Long projectId, ProductRequest request) {
        OrganizationMember caller = require(customerId, MANAGE_PRODUCT_ACCESS);
        OrganizationGroup project = project(caller, projectId);
        if (request == null || request.productId() == null) {
            throw new IllegalArgumentException("Choose a product.");
        }
        String role = request.productRole() == null ? "" : request.productRole().trim();
        if (role.isEmpty() || role.length() > 50) {
            throw new IllegalArgumentException("Enter a product role (up to 50 characters).");
        }
        Product product = productRepository.findById(request.productId())
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("This product is not currently available."));
        subscriptionRepository.findByOwnerOrganizationIdAndProductId(caller.getOrganizationId(), product.getId())
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Your organization does not have an active subscription for this product."));

        Optional<OrganizationGroupProduct> existing = groupProductRepository.findByGroupIdAndProductId(projectId, product.getId());
        OrganizationGroupProduct row = existing.orElseGet(OrganizationGroupProduct::new);
        row.setGroupId(projectId);
        row.setProductId(product.getId());
        row.setProductRole(role);
        groupProductRepository.save(row);
        groupProductRepository.flush();
        audit(existing.isPresent() ? "PROJECT_PRODUCT_CHANGED" : "PROJECT_PRODUCT_ADDED", customerId, project,
            (existing.isPresent() ? "Set role " : "Added product " + product.getId() + " with role ") + role + " in project " + project.getName());
        recomputeAll(projectId, customerId);
        return toDto(project);
    }

    public ProjectDto removeProduct(Long customerId, Long projectId, Long productId) {
        OrganizationMember caller = require(customerId, MANAGE_PRODUCT_ACCESS);
        OrganizationGroup project = project(caller, projectId);
        groupProductRepository.findByGroupIdAndProductId(projectId, productId).ifPresent(row -> {
            groupProductRepository.delete(row);
            groupProductRepository.flush();
            audit("PROJECT_PRODUCT_REMOVED", customerId, project, "Removed product " + productId + " from project " + project.getName());
            recomputeAll(projectId, customerId);
        });
        return toDto(project);
    }

    // ------------------------------------------------------------------ effective access (BR-PRJ-004 to 006)

    private void recomputeAll(Long projectId, Long actorCustomerId) {
        groupMemberRepository.findByGroupId(projectId).forEach(gm ->
            memberRepository.findById(gm.getOrganizationMemberId()).ifPresent(m -> recompute(m, actorCustomerId)));
    }

    /**
     * Brings the project-sourced product access of one member in line with the projects they belong to.
     * A direct assignment (no source) is never touched. A source that goes away leaves the access in place when
     * another project still gives the product; otherwise it becomes INACTIVE with its source cleared.
     */
    private void recompute(OrganizationMember member, Long actorCustomerId) {
        Map<Long, OrganizationGroupProduct> wanted = new LinkedHashMap<>();
        Map<Long, Long> wantedFrom = new HashMap<>();
        if (member.getStatus() == MembershipStatus.ACTIVE) {
            groupMemberRepository.findByOrganizationMemberId(member.getId()).stream()
                .map(OrganizationGroupMember::getGroupId).sorted().distinct()
                .map(groupRepository::findById).flatMap(Optional::stream)
                .filter(g -> g.getKind() == GroupKind.PROJECT && g.getOrganizationId().equals(member.getOrganizationId()))
                .forEach(g -> groupProductRepository.findByGroupId(g.getId()).forEach(gp -> {
                    if (!wanted.containsKey(gp.getProductId())) {
                        wanted.put(gp.getProductId(), gp);
                        wantedFrom.put(gp.getProductId(), g.getId());
                    }
                }));
        }
        Map<Long, OrganizationProductAccess> rows = accessRepository.findByOrganizationMemberId(member.getId()).stream()
            .collect(Collectors.toMap(OrganizationProductAccess::getProductId, a -> a, (a, b) -> a));

        for (OrganizationProductAccess row : rows.values()) {
            if (row.getSourceGroupId() == null || row.getStatus() != MembershipStatus.ACTIVE) {
                continue;
            }
            OrganizationGroupProduct keep = wanted.get(row.getProductId());
            if (keep == null) {
                row.setStatus(MembershipStatus.INACTIVE);
                row.setSourceGroupId(null);
                accessRepository.save(row);
                auditAccess("PRODUCT_ACCESS_REVOKED", actorCustomerId, member, row.getProductId(), "Project access ended");
            } else {
                row.setProductRole(keep.getProductRole());
                row.setSourceGroupId(wantedFrom.get(row.getProductId()));
                accessRepository.save(row);
            }
        }
        for (Map.Entry<Long, OrganizationGroupProduct> e : wanted.entrySet()) {
            OrganizationProductAccess row = rows.get(e.getKey());
            if (row != null && row.getStatus() == MembershipStatus.ACTIVE) {
                continue; // direct wins, or already kept above
            }
            if (row == null) {
                row = new OrganizationProductAccess();
                row.setOrganizationMemberId(member.getId());
                row.setProductId(e.getKey());
            }
            row.setProductRole(e.getValue().getProductRole());
            row.setStatus(MembershipStatus.ACTIVE);
            row.setSourceGroupId(wantedFrom.get(e.getKey()));
            row.setAssignedAt(Instant.now());
            accessRepository.save(row);
            auditAccess("PRODUCT_ACCESS_GRANTED", actorCustomerId, member, e.getKey(),
                "Granted " + e.getValue().getProductRole() + " through a project");
            notifyGain(member, e.getKey(), e.getValue().getProductRole(), wantedFrom.get(e.getKey()));
        }
    }

    /** BR-PRJ-008: once per gain; a failed notice is only logged. */
    private void notifyGain(OrganizationMember member, Long productId, String role, Long projectId) {
        try {
            Optional<Product> product = productRepository.findById(productId);
            String project = groupRepository.findById(projectId).map(OrganizationGroup::getName).orElse("a project");
            customerRepository.findById(member.getCustomerId()).ifPresent(c ->
                notificationService.notifyTemplated(c.getId(), c.getEmail(), NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                    "NOTIF_PROJECT_ACCESS_GRANTED", Map.of("product", product.map(Product::getName).orElse("product " + productId),
                        "role", String.valueOf(role), "project", project)));
        } catch (RuntimeException e) {
            log.warn("Could not notify member {} of project access: {}", member.getId(), e.getMessage());
        }
    }

    // ------------------------------------------------------------------ internals

    private OrganizationMember caller(Long customerId) {
        OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new ForbiddenException(NO_PERMISSION));
        Organization org = organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ForbiddenException(NO_PERMISSION));
        if (!authorizationService.organizationInGoodStanding(org.getStatus())
            || org.getLifecycleStatus() != OrganizationLifecycleStatus.ACTIVE) {
            throw new ForbiddenException("Your organization's account is not currently active");
        }
        return member;
    }

    private OrganizationMember require(Long customerId, String permission) {
        return requireAny(customerId, permission);
    }

    private OrganizationMember requireAny(Long customerId, String... permissions) {
        OrganizationMember member = caller(customerId);
        for (String permission : permissions) {
            if (memberAccessService.hasPermission(member, permission)) {
                return member;
            }
        }
        throw new ForbiddenException(NO_PERMISSION);
    }

    private OrganizationGroup project(OrganizationMember caller, Long projectId) {
        return groupRepository.findById(projectId)
            .filter(g -> g.getKind() == GroupKind.PROJECT && g.getOrganizationId().equals(caller.getOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
    }

    private void requireUniqueName(Long organizationId, String name, Long exceptId) {
        boolean taken = groupRepository.findByOrganizationIdAndKind(organizationId, GroupKind.PROJECT).stream()
            .anyMatch(g -> !g.getId().equals(exceptId) && g.getName().equalsIgnoreCase(name));
        if (taken) {
            throw new DuplicateResourceException("A project with this name already exists.");
        }
    }

    private static String cleanName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty() || name.length() > 150) {
            throw new IllegalArgumentException("Project name is required (up to 150 characters).");
        }
        return name;
    }

    private static String cleanDescription(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.length() > 500) {
            throw new IllegalArgumentException("Description can have up to 500 characters.");
        }
        return text.isEmpty() ? null : text;
    }

    private void audit(String action, Long customerId, OrganizationGroup project, String detail) {
        auditService.recordSuccess(action, null, customerId, null, "OrganizationProject", String.valueOf(project.getId()),
            project.getOrganizationId(), detail);
    }

    private void auditAccess(String action, Long actorCustomerId, OrganizationMember member, Long productId, String detail) {
        auditService.recordSuccess(action, null, actorCustomerId, null, "OrganizationProductAccess",
            productId + ":" + member.getId(), member.getOrganizationId(), detail + " (product " + productId + ", member " + member.getId() + ")");
    }

    private ProjectDto toDto(OrganizationGroup project) {
        List<ProjectMemberDto> members = groupMemberRepository.findByGroupId(project.getId()).stream()
            .map(gm -> memberRepository.findById(gm.getOrganizationMemberId())).flatMap(Optional::stream)
            .map(m -> {
                Customer c = customerRepository.findById(m.getCustomerId()).orElse(null);
                String name = c == null ? "" : ((c.getFirstName() == null ? "" : c.getFirstName()) + " "
                    + (c.getLastName() == null ? "" : c.getLastName())).trim();
                return new ProjectMemberDto(m.getId(), m.getCustomerId(), name, c == null ? null : c.getEmail(), m.getOrgRole());
            })
            .sorted(Comparator.comparing(m -> m.name().toLowerCase(Locale.ROOT))).toList();
        List<ProjectProductDto> products = groupProductRepository.findByGroupId(project.getId()).stream()
            .map(gp -> new ProjectProductDto(gp.getProductId(),
                productRepository.findById(gp.getProductId()).map(Product::getName).orElse("Product " + gp.getProductId()),
                gp.getProductRole()))
            .sorted(Comparator.comparing(p -> p.productName().toLowerCase(Locale.ROOT))).toList();
        return new ProjectDto(project.getId(), project.getName(), project.getDescription(), project.getCreatedAt(), members, products);
    }
}
