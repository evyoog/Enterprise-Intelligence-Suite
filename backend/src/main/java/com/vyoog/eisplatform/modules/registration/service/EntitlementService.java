package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckRequest;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckResponse;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementDto;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * REQ-SUB-002 (C52, C97 EN-1 to EN-5): entitlements are <b>read-only and derived</b>. A product is available when there is an ACTIVE subscription to
 * an ACTIVE catalogue product, the plan's included features say so and, for an organization member, the member has ACTIVE access to that product.
 * Nothing is granted, revoked, stored or counted here: a suspended, cancelled or expired subscription grants nothing (BR-1), a plan change shows at
 * once (BR-2), and only the plan's limit is exposed, never consumption (EN-5).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntitlementService {

    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository planRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    /** Entitlements of an individual customer's own subscriptions, plus, when the customer is an active member of an organization, the
     * organization's subscriptions to the products the member has access to. */
    public List<EntitlementDto> forCustomer(Long customerId) {
        List<EntitlementDto> result = new ArrayList<>();
        subscriptionRepository.findByOwnerCustomerId(customerId).stream().filter(this::grantsAccess).forEach(s -> toDto(s).ifPresent(result::add));
        memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE).ifPresent(member ->
            subscriptionRepository.findByOwnerOrganizationId(member.getOrganizationId()).stream()
                .filter(this::grantsAccess).filter(s -> memberHasAccess(member, s.getProductId()))
                .forEach(s -> toDto(s).ifPresent(result::add)));
        return result;
    }

    /** The organization's entitlements: all its ACTIVE subscriptions. */
    public List<EntitlementDto> forOrganization(Long organizationId) {
        return subscriptionRepository.findByOwnerOrganizationId(organizationId).stream().filter(this::grantsAccess)
            .map(this::toDto).flatMap(Optional::stream).toList();
    }

    /** C97 EN-3: platform-admin lookup, read-only, recorded in the audit log. */
    @Transactional
    public List<EntitlementDto> adminLookupCustomer(Long adminCustomerId, Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        auditService.recordSuccess("ENTITLEMENTS_VIEWED", null, adminCustomerId, null, "Customer", customerId.toString(), null,
            "Entitlements looked up for support");
        return forCustomer(customerId);
    }

    @Transactional
    public List<EntitlementDto> adminLookupOrganization(Long adminCustomerId, Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        auditService.recordSuccess("ENTITLEMENTS_VIEWED", null, adminCustomerId, null, "Organization", organizationId.toString(), organizationId,
            "Entitlements looked up for support");
        return forOrganization(organizationId);
    }

    /** C97 EN-2: the check hosted products call. Never throws for a normal "no": the answer is {@code allowed=false} with a reason. */
    public EntitlementCheckResponse check(EntitlementCheckRequest request) {
        Long productId = request.productId();
        if (request.customerId() == null && request.organizationId() == null) {
            return EntitlementCheckResponse.deny("BAD_REQUEST", productId);
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
            return EntitlementCheckResponse.deny("UNKNOWN_PRODUCT", productId);
        }
        ProductSubscription subscription = null;
        boolean memberAccessMissing = false;
        if (request.organizationId() != null) {
            subscription = subscriptionRepository.findByOwnerOrganizationIdAndProductId(request.organizationId(), productId).filter(this::grantsAccess).orElse(null);
            if (subscription != null && request.customerId() != null) {
                OrganizationMember member = memberRepository.findByOrganizationIdAndCustomerId(request.organizationId(), request.customerId())
                    .filter(m -> m.getStatus() == MembershipStatus.ACTIVE).orElse(null);
                if (member == null || !memberHasAccess(member, productId)) {
                    subscription = null;
                    memberAccessMissing = true;
                }
            }
        } else {
            subscription = subscriptionRepository.findByOwnerCustomerIdAndProductId(request.customerId(), productId).filter(this::grantsAccess).orElse(null);
            if (subscription == null) {
                OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(request.customerId(), MembershipStatus.ACTIVE).orElse(null);
                if (member != null) {
                    ProductSubscription orgSubscription = subscriptionRepository
                        .findByOwnerOrganizationIdAndProductId(member.getOrganizationId(), productId).filter(this::grantsAccess).orElse(null);
                    if (orgSubscription != null) {
                        if (memberHasAccess(member, productId)) {
                            subscription = orgSubscription;
                        } else {
                            memberAccessMissing = true;
                        }
                    }
                }
            }
        }
        if (subscription == null) {
            return EntitlementCheckResponse.deny(memberAccessMissing ? "NO_MEMBER_ACCESS" : "NO_SUBSCRIPTION", productId);
        }
        EntitlementDto e = toDto(subscription).orElse(null);
        if (e == null) {
            return EntitlementCheckResponse.deny("NO_SUBSCRIPTION", productId);
        }
        String feature = request.featureKey() == null ? "" : request.featureKey().trim();
        if (!feature.isEmpty() && e.includedFeatures().stream().noneMatch(f -> f.equalsIgnoreCase(feature))) {
            return new EntitlementCheckResponse(false, "FEATURE_NOT_INCLUDED", productId, e.planName(), e.ownerType(), e.expiresAt(),
                e.includedFeatures(), e.usageLimit(), e.accessMode(), e.graceEndsAt());
        }
        return new EntitlementCheckResponse(true, null, productId, e.planName(), e.ownerType(), e.expiresAt(), e.includedFeatures(), e.usageLimit(),
            e.accessMode(), e.graceEndsAt());
    }

    /** BR-1: only an ACTIVE subscription that has not passed its term end grants anything. */
    private boolean isLive(ProductSubscription s) {
        return s.getStatus() == SubscriptionStatus.ACTIVE && (s.getExpiresAt() == null || s.getExpiresAt().isAfter(Instant.now()));
    }

    /** C100 RR-1 B: after its term ends with auto-renew off, an ACTIVE subscription is read-only until its grace period ends. */
    private boolean inGrace(ProductSubscription s) {
        Instant now = Instant.now();
        Instant graceEnd = graceEnd(s);
        return s.getStatus() == SubscriptionStatus.ACTIVE && s.getExpiresAt() != null && !s.getExpiresAt().isAfter(now)
            && graceEnd != null && graceEnd.isAfter(now);
    }

    /** The grace end the expiry job has set, or, before the job has run (it is hourly), the one it will set: the term end plus the grace days, for a
     * subscription with auto-renew off. */
    private Instant graceEnd(ProductSubscription s) {
        if (s.getGraceEndsAt() != null) {
            return s.getGraceEndsAt();
        }
        return s.isAutoRenew() || s.getExpiresAt() == null ? null : s.getExpiresAt().plus(SubscriptionService.GRACE_DAYS, java.time.temporal.ChronoUnit.DAYS);
    }

    private boolean grantsAccess(ProductSubscription s) {
        return isLive(s) || inGrace(s);
    }

    private String accessMode(ProductSubscription s) {
        return inGrace(s) ? "READ_ONLY" : "FULL";
    }

    private boolean memberHasAccess(OrganizationMember member, Long productId) {
        return accessRepository.findByOrganizationMemberIdAndProductId(member.getId(), productId)
            .map(a -> a.getStatus() == MembershipStatus.ACTIVE).orElse(false);
    }

    private Optional<EntitlementDto> toDto(ProductSubscription s) {
        Product product = productRepository.findById(s.getProductId()).filter(p -> p.getStatus() == ProductStatus.ACTIVE).orElse(null);
        if (product == null) {
            return Optional.empty();
        }
        ProductPlan plan = s.getPlanId() == null ? null : planRepository.findById(s.getPlanId()).orElse(null);
        List<String> features = plan == null || plan.getIncludedFeatures() == null ? List.of()
            : Arrays.stream(plan.getIncludedFeatures().split(",")).map(String::trim).filter(f -> !f.isEmpty()).toList();
        return Optional.of(new EntitlementDto(s.getId(), product.getId(), product.getName(),
            s.getOwnerType() == RegistrationOwnerType.ORGANIZATION ? "ORGANIZATION" : "INDIVIDUAL",
            plan == null ? null : plan.getId(), plan == null ? null : plan.getName(), s.getStatus().name(), s.getExpiresAt(),
            features, plan == null ? null : plan.getUsageLimit(), s.getQuantity(), accessMode(s), inGrace(s) ? graceEnd(s) : null));
    }
}
