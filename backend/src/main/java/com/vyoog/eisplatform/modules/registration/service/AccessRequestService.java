package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.access.service.AccessSyncService;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.AccessRequestDto;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.CreateAccessRequest;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.DecideAccessRequest;
import com.vyoog.eisplatform.modules.registration.model.AccessRequest;
import com.vyoog.eisplatform.modules.registration.model.AccessRequestStatus;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.repository.AccessRequestRepository;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * C103 / REQ-TEN-010: a member asks for personal access to a product the organization already subscribes to, when they
 * do not have it — the gap AP-1 found next to the admin-only {@code AccessManagementService}. Approving calls the same
 * {@link AccessSyncService#grantProduct} an administrator's own grant uses (same seat check, same audit trail);
 * rejecting needs a reason. One pending request per member and product (BR, backed by a DB unique index).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessRequestService {

    private final AccessRequestRepository requestRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationSelfService organizationSelfService;
    private final MemberAccessService memberAccessService;
    private final AccessSyncService syncService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public AccessRequestDto request(Long customerId, CreateAccessRequest request) {
        OrganizationMember member = organizationSelfService.requireCaller(customerId);
        if (request == null || request.productId() == null) {
            throw new IllegalArgumentException("Choose a product.");
        }
        Product product = productRepository.findById(request.productId()).filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (subscriptionRepository.findByOwnerOrganizationIdAndProductId(member.getOrganizationId(), product.getId())
                .map(ProductSubscription::getStatus).filter(s -> s == com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus.ACTIVE).isEmpty()) {
            throw new IllegalArgumentException("Your organization does not have an active subscription for " + product.getName() + ".");
        }
        boolean alreadyHasAccess = accessRepository.findByOrganizationMemberIdAndProductId(member.getId(), product.getId())
            .map(a -> a.getStatus() == MembershipStatus.ACTIVE).orElse(false);
        if (alreadyHasAccess) {
            throw new IllegalArgumentException("You already have access to " + product.getName() + ".");
        }
        if (requestRepository.findByOrganizationMemberIdAndProductIdAndStatus(member.getId(), product.getId(), AccessRequestStatus.PENDING).isPresent()) {
            throw new IllegalArgumentException("You already have a pending request for " + product.getName() + ".");
        }
        AccessRequest entity = new AccessRequest();
        entity.setOrganizationMemberId(member.getId());
        entity.setProductId(product.getId());
        entity.setMessage(clean(request.message()));
        entity = requestRepository.save(entity);
        auditService.recordSuccess("ACCESS_REQUESTED", null, customerId, null, "AccessRequest", entity.getId().toString(), member.getOrganizationId(),
            "Requested access to " + product.getName());
        notifyApprovers(member.getOrganizationId(), "New access request",
            requesterName(customerId) + " asked for access to " + product.getName() + (entity.getMessage() == null ? "." : ": " + entity.getMessage()));
        return toDto(entity);
    }

    public java.util.List<AccessRequestDto> myRequests(Long customerId) {
        OrganizationMember member = organizationSelfService.requireCaller(customerId);
        return requestRepository.findByOrganizationMemberIdOrderByCreatedAtDesc(member.getId()).stream().map(this::toDto).toList();
    }

    @Transactional
    public AccessRequestDto cancel(Long customerId, Long requestId) {
        OrganizationMember member = organizationSelfService.requireCaller(customerId);
        AccessRequest entity = find(requestId);
        if (!entity.getOrganizationMemberId().equals(member.getId())) {
            throw new ResourceNotFoundException("Access request not found");
        }
        if (entity.getStatus() != AccessRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only a pending request can be cancelled.");
        }
        entity.setStatus(AccessRequestStatus.CANCELLED);
        entity = requestRepository.save(entity);
        return toDto(entity);
    }

    // ------------------------------------------------------------------ administrators (Manage product access)

    public java.util.List<AccessRequestDto> listForOrganization(Long customerId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_PRODUCT_ACCESS");
        return requestRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream().map(this::toDto).toList();
    }

    @Transactional
    public AccessRequestDto approve(Long customerId, Long requestId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_PRODUCT_ACCESS");
        AccessRequest entity = requirePendingInOrganization(organizationId, requestId);
        OrganizationMember target = memberRepository.findById(entity.getOrganizationMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        Product product = productRepository.findById(entity.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        syncService.grantProduct(target, product.getId(), null, customerId);   // same seat check and audit as an administrator's own grant
        entity.setStatus(AccessRequestStatus.APPROVED);
        entity.setDecidedByCustomerId(customerId);
        entity.setDecidedAt(Instant.now());
        entity = requestRepository.save(entity);
        auditService.recordSuccess("ACCESS_REQUEST_APPROVED", null, customerId, null, "AccessRequest", requestId.toString(), organizationId,
            "Access to " + product.getName() + " granted");
        customerRepository.findById(target.getCustomerId()).ifPresent(c ->
            notificationService.notifyTemplated(c.getId(), c.getEmail(), NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                "NOTIF_ACCESS_CHANGED", Map.of("changes", "You can now open " + product.getName() + ".")));
        return toDto(entity);
    }

    @Transactional
    public AccessRequestDto reject(Long customerId, Long requestId, DecideAccessRequest request) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_PRODUCT_ACCESS");
        AccessRequest entity = requirePendingInOrganization(organizationId, requestId);
        String note = request == null || request.note() == null ? "" : request.note().trim();
        if (note.isEmpty()) {
            throw new IllegalArgumentException("Write a reason for rejecting this request.");
        }
        Product product = productRepository.findById(entity.getProductId()).orElse(null);
        entity.setStatus(AccessRequestStatus.REJECTED);
        entity.setDecidedByCustomerId(customerId);
        entity.setDecidedAt(Instant.now());
        entity.setDecisionNote(cut(note));
        entity = requestRepository.save(entity);
        auditService.recordSuccess("ACCESS_REQUEST_REJECTED", null, customerId, null, "AccessRequest", requestId.toString(), organizationId,
            "Access request rejected: " + note);
        memberRepository.findById(entity.getOrganizationMemberId()).ifPresent(target ->
            customerRepository.findById(target.getCustomerId()).ifPresent(c ->
                notificationService.notify(c.getId(), c.getEmail(), NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                    "Access request declined", "Your request for access to " + (product == null ? "a product" : product.getName()) + " was declined: " + note)));
        return toDto(entity);
    }

    // ------------------------------------------------------------------ self-service removal (not a request: immediate)

    /** C103: a member removes their own access to a product at once; getting it back needs a new request. Releases the seat immediately. */
    @Transactional
    public void removeMyAccess(Long customerId, Long productId) {
        OrganizationMember member = organizationSelfService.requireCaller(customerId);
        OrganizationProductAccess row = accessRepository.findByOrganizationMemberIdAndProductId(member.getId(), productId)
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("You do not have access to this product."));
        Product product = productRepository.findById(productId).orElse(null);
        syncService.revokeProduct(member, productId, customerId);
        auditService.recordSuccess("ACCESS_SELF_REMOVED", null, customerId, null, "OrganizationProductAccess", row.getId().toString(), member.getOrganizationId(),
            "Removed own access to " + (product == null ? "a product" : product.getName()));
    }

    // ------------------------------------------------------------------ helpers

    private AccessRequest requirePendingInOrganization(Long organizationId, Long requestId) {
        AccessRequest entity = find(requestId);
        OrganizationMember member = memberRepository.findById(entity.getOrganizationMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Access request not found"));
        if (!member.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        if (entity.getStatus() != AccessRequestStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been decided.");
        }
        return entity;
    }

    private AccessRequest find(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Access request not found"));
    }

    private void notifyApprovers(Long organizationId, String title, String message) {
        memberRepository.findByOrganizationId(organizationId).stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .filter(m -> m.getOrgRole() == com.vyoog.eisplatform.modules.registration.model.OrgRole.ORG_ADMIN || memberAccessService.hasPermission(m, "MANAGE_PRODUCT_ACCESS"))
            .forEach(m -> customerRepository.findById(m.getCustomerId()).ifPresent(c ->
                notificationService.notify(c.getId(), c.getEmail(), NotificationCategory.ORGANIZATION, NotificationSeverity.INFO, title, message)));
    }

    private String requesterName(Long customerId) {
        return customerRepository.findById(customerId).map(c -> (c.getFirstName() + " " + c.getLastName()).trim()).orElse("A member");
    }

    private AccessRequestDto toDto(AccessRequest entity) {
        OrganizationMember member = memberRepository.findById(entity.getOrganizationMemberId()).orElse(null);
        Customer memberCustomer = member == null ? null : customerRepository.findById(member.getCustomerId()).orElse(null);
        Product product = productRepository.findById(entity.getProductId()).orElse(null);
        Customer decider = entity.getDecidedByCustomerId() == null ? null : customerRepository.findById(entity.getDecidedByCustomerId()).orElse(null);
        return new AccessRequestDto(entity.getId(), entity.getProductId(), product == null ? "Unknown product" : product.getName(),
            entity.getOrganizationMemberId(), memberCustomer == null ? "Unknown member" : (memberCustomer.getFirstName() + " " + memberCustomer.getLastName()).trim(),
            entity.getStatus().name(), entity.getMessage(), entity.getCreatedAt(),
            decider == null ? null : (decider.getFirstName() + " " + decider.getLastName()).trim(), entity.getDecidedAt(), entity.getDecisionNote());
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cut(value.trim());
    }

    private static String cut(String value) {
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
