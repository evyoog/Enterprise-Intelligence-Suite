package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest;
import com.vyoog.eisplatform.modules.registration.dto.MyProductDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Individual customer's own products/subscriptions — everything here is
 * scoped to a single {@code ownerCustomerId} passed in by the controller
 * (resolved from the caller's own JWT `sub`, never trusted from the request
 * body — see MeController).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository organizationRepository;
    private final com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository memberRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    /** REQ-BIL-001.2: generating an invoice is a side effect of subscribing
     * or renewing — called directly, same pattern as {@code OrderService}
     * already calling this class directly for provisioning. Does NOT change
     * when or whether a subscription activates (FRD Open question 3, still
     * open — decision C47). */
    private final InvoiceService invoiceService;
    /** REQ-INT-002 (C62): lifecycle events, written in the same transaction. */
    private final OutboxService outboxService;

    /** REQ-INT-002.8: IDs, status and dates only (BR-2). */
    private void publish(String eventType, ProductSubscription subscription) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getId());
        payload.put("productId", subscription.getProductId());
        payload.put("planId", subscription.getPlanId());
        payload.put("ownerType", subscription.getOwnerType() == null ? null : subscription.getOwnerType().name());
        payload.put("ownerCustomerId", subscription.getOwnerCustomerId());
        payload.put("ownerOrganizationId", subscription.getOwnerOrganizationId());
        payload.put("status", subscription.getStatus() == null ? null : subscription.getStatus().name());
        payload.put("expiresAt", subscription.getExpiresAt() == null ? null : subscription.getExpiresAt().toString());
        outboxService.publish(eventType, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, subscription.getId(), payload);
    }

    /** Every mutation below starts here — resolves the subscription and
     * confirms it belongs to this individual customer, or refuses with the
     * same 404 a nonexistent id would give (never confirming that some
     * OTHER customer's subscription exists at this id). */
    private ProductSubscription resolveOwnSubscription(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if (subscription.getOwnerType() != RegistrationOwnerType.INDIVIDUAL
                || !customerId.equals(subscription.getOwnerCustomerId())) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        return subscription;
    }

    public List<MyProductDto> listMyProducts(Long customerId) {
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        Map<Long, ProductSubscription> byProductId = subscriptionRepository.findByOwnerCustomerId(customerId).stream()
            .collect(Collectors.toMap(ProductSubscription::getProductId, s -> s, (a, b) -> a));

        return activeProducts.stream()
            .map(product -> {
                ProductSubscription subscription = byProductId.get(product.getId());
                return new MyProductDto(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    subscription == null ? null : subscription.getStatus()
                );
            })
            .toList();
    }

    public List<SubscriptionDto> listMySubscriptions(Long customerId) {
        return subscriptionRepository.findByOwnerCustomerId(customerId).stream()
            .map(this::toDto)
            .toList();
    }

    /** Phase 19: the business dashboard's own billing section — same
     * mapping as listMySubscriptions, just organization-owned rows instead
     * of individual-owned ones. */
    public List<SubscriptionDto> listOrganizationSubscriptions(Long organizationId) {
        return subscriptionRepository.findByOwnerOrganizationId(organizationId).stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * No payment integration exists yet (see ProductSubscription's own
     * javadoc) — subscribing goes straight to ACTIVE rather than pretending
     * a payment happened. This is the seam a real payment step slots into
     * later: insert it between this method's validation and the
     * status/startedAt assignment below, without changing the API shape.
     */
    @Transactional
    public SubscriptionDto subscribe(Long customerId, Long productId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerCustomerIdAndProductId(customerId, productId)
            .orElseGet(() -> {
                ProductSubscription created = new ProductSubscription();
                created.setProductId(productId);
                created.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
                created.setOwnerCustomerId(customerId);
                return created;
            });
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(null);
        subscription = subscriptionRepository.save(subscription);

        customerRepository.findById(customerId).ifPresent(customer ->
            notificationService.notify(customerId, customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO,
                "Subscribed to " + product.getName(),
                "You're now subscribed to " + product.getName() + "."));
        auditService.recordSuccess("SUBSCRIPTION_CREATED", null, customerId, null,
            "ProductSubscription", productId.toString(), null, "Customer subscribed to product " + productId);
        publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        invoiceService.generateForSubscription(subscription.getId());

        return toDto(subscription, product.getName());
    }

    /** C59 (REQ-MKT-003.8): an individual's cart checkout. Same as
     * {@link #subscribe}, but with the cart item's plan, and without
     * issuing an invoice — the cart checkout issues one invoice for every
     * item ({@code InvoiceService#generateForSubscriptions}). Returns the
     * subscription id. */
    @Transactional
    public Long subscribeFromCart(Long customerId, Long productId, Long planId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerCustomerIdAndProductId(customerId, productId)
            .orElseGet(() -> {
                ProductSubscription created = new ProductSubscription();
                created.setProductId(productId);
                created.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
                created.setOwnerCustomerId(customerId);
                return created;
            });
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setPlanId(planId);
        // REQ-SUB-004.1 (C64): a Monthly/Yearly plan gets a renewal date and auto-renew.
        subscription.setExpiresAt(firstRenewalDate(planId, subscription.getStartedAt()));
        subscription.setAutoRenew(true);
        subscription = subscriptionRepository.save(subscription);

        customerRepository.findById(customerId).ifPresent(customer ->
            notificationService.notify(customerId, customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO,
                "Subscribed to " + product.getName(),
                "You're now subscribed to " + product.getName() + "."));
        auditService.recordSuccess("SUBSCRIPTION_CREATED", null, customerId, null,
            "ProductSubscription", productId.toString(), null, "Customer subscribed to product " + productId + " from the cart");
        publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        return subscription.getId();
    }

    /** 09.02.01 Service Provisioning (sprint 2027.1.1): the organization
     * equivalent of {@link #subscribe} — called by {@code OrderService} once
     * an order is approved, never directly by a member (there is no
     * self-serve organization-subscribe endpoint; every org subscription
     * goes through an order). Unlike {@code subscribe}, this accepts a plan
     * up front, since the order that triggers it already carries one. */
    @Transactional
    public SubscriptionDto subscribeOrganization(Long organizationId, Long productId, Long planId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerOrganizationIdAndProductId(organizationId, productId)
            .orElseGet(() -> {
                ProductSubscription created = new ProductSubscription();
                created.setProductId(productId);
                created.setOwnerType(RegistrationOwnerType.ORGANIZATION);
                created.setOwnerOrganizationId(organizationId);
                return created;
            });
        if (subscription.getId() == null) {
            // REQ-SUB-003 engineering default: start at the organization's
            // licensed seats (at least the active members, at least 1).
            int licensed = organizationRepository.findById(organizationId).map(o -> o.getLicensedSeats()).orElse(0);
            long active = memberRepository.countByOrganizationIdAndStatus(organizationId,
                com.vyoog.eisplatform.modules.registration.model.MembershipStatus.ACTIVE);
            subscription.setQuantity((int) Math.min(SubscriptionSeatService.MAX_SEATS, Math.max(1, Math.max(licensed, active))));
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setPlanId(planId);
        // REQ-SUB-004.1 (C64): a Monthly/Yearly plan gets a renewal date and auto-renew.
        subscription.setExpiresAt(firstRenewalDate(planId, subscription.getStartedAt()));
        subscription.setAutoRenew(true);
        subscription = subscriptionRepository.save(subscription);

        auditService.recordSuccess("SUBSCRIPTION_CREATED", null, null, null,
            "ProductSubscription", productId.toString(), organizationId, "Organization subscribed to product " + productId);
        publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        invoiceService.generateForSubscription(subscription.getId());

        return toDto(subscription, product.getName());
    }

    private SubscriptionDto toDto(ProductSubscription subscription) {
        String productName = productRepository.findById(subscription.getProductId())
            .map(Product::getName)
            .orElse("Unknown product");
        return toDto(subscription, productName);
    }

    private SubscriptionDto toDto(ProductSubscription subscription, String productName) {
        String planName = subscription.getPlanId() == null ? null
            : productPlanRepository.findById(subscription.getPlanId()).map(ProductPlan::getName).orElse(null);
        return new SubscriptionDto(
            subscription.getId(),
            subscription.getProductId(),
            productName,
            subscription.getStatus(),
            subscription.getStartedAt(),
            subscription.getExpiresAt(),
            subscription.getPlanId(),
            planName,
            subscription.getQuantity(),
            subscription.isAutoRenew()
        );
    }

    private void notify(ProductSubscription subscription, String title, String message) {
        if (subscription.getOwnerCustomerId() == null) {
            return; // 07.01 Subscription Lifecycle (2026.4.3): organization-owned
            // subscriptions aren't notified yet — see this feature's own FRD.
        }
        customerRepository.findById(subscription.getOwnerCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO, title, message));
    }

    /** 07.01.01 Suspend subscription (sprint 2026.4.3): reversible, unlike
     * cancel — see SubscriptionStatus's own javadoc. */
    @Transactional
    public SubscriptionDto suspendSubscription(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an active subscription can be suspended.");
        }
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription = subscriptionRepository.save(subscription);
        notify(subscription, "Subscription suspended", "Your subscription has been suspended.");
        auditService.recordSuccess("SUBSCRIPTION_SUSPENDED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null, "Subscription suspended");
        publish(PlatformEventTypes.SUBSCRIPTION_SUSPENDED, subscription);
        return toDto(subscription);
    }

    /** 07.01.01 Reactivate subscription (07.04.02, sprint 2026.4.3). */
    @Transactional
    public SubscriptionDto reactivateSubscription(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        if (subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new IllegalArgumentException("Only a suspended subscription can be reactivated.");
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);
        notify(subscription, "Subscription reactivated", "Your subscription has been reactivated.");
        auditService.recordSuccess("SUBSCRIPTION_REACTIVATED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null, "Subscription reactivated");
        publish(PlatformEventTypes.SUBSCRIPTION_RESUMED, subscription);
        return toDto(subscription);
    }

    /** 07.01.01 Cancel subscription (sprint 2026.4.3) — one-way, same
     * reasoning as {@code OrganizationMemberService#removeMember}. */
    @Transactional
    public SubscriptionDto cancelSubscription(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("This subscription is already cancelled.");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);
        notify(subscription, "Subscription cancelled", "Your subscription has been cancelled.");
        auditService.recordSuccess("SUBSCRIPTION_CANCELLED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null, "Subscription cancelled");
        publish(PlatformEventTypes.SUBSCRIPTION_CANCELLED, subscription);
        return toDto(subscription);
    }

    /** REQ-SUB-004.1: the end of the first billing period, or null for a
     * one-time plan or no plan (no renewal date, no reminders). */
    private Instant firstRenewalDate(Long planId, Instant start) {
        BillingPeriod period = planId == null ? null
            : productPlanRepository.findById(planId).map(ProductPlan::getBillingPeriod).orElse(null);
        if (period == BillingPeriod.MONTHLY) {
            return start.plus(30, ChronoUnit.DAYS);
        }
        if (period == BillingPeriod.YEARLY) {
            return start.plus(365, ChronoUnit.DAYS);
        }
        return null;
    }

    /** REQ-SUB-004.9: what a renewal reminder says about a subscription. */
    public record RenewalInfo(String productName, String planName, java.math.BigDecimal amount, String currency) {
    }

    public RenewalInfo renewalInfo(ProductSubscription subscription) {
        Product product = productRepository.findById(subscription.getProductId()).orElse(null);
        ProductPlan plan = subscription.getPlanId() == null ? null : productPlanRepository.findById(subscription.getPlanId()).orElse(null);
        java.math.BigDecimal amount = plan != null ? plan.getPrice() : product != null ? product.getPrice() : null;
        String currency = plan != null && plan.getCurrency() != null ? plan.getCurrency().name() : "USD";
        return new RenewalInfo(product == null ? "Unknown product" : product.getName(), plan == null ? null : plan.getName(), amount, currency);
    }

    /**
     * REQ-SUB-004.2 (C64, BR-2): renews one ACTIVE, auto-renewing subscription
     * whose renewal date has come — the term moves one period on from the old
     * renewal date, a renewal invoice is generated (charging a saved method
     * automatically is not possible yet, see {@code RenewalService}),
     * {@code SubscriptionRenewed} is published and the owner is notified.
     * Returns false when nothing was due (already renewed, or no period).
     */
    @Transactional
    public boolean autoRenew(Long subscriptionId) {
        ProductSubscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null || subscription.getStatus() != SubscriptionStatus.ACTIVE || !subscription.isAutoRenew()
                || subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(Instant.now())) {
            return false;
        }
        Instant oldRenewal = subscription.getExpiresAt();
        Optional<Instant> next = nextExpiry(subscription, oldRenewal);
        if (next.isEmpty()) {
            return false;
        }
        subscription.setExpiresAt(next.get());
        subscription = subscriptionRepository.save(subscription);
        invoiceService.generateForSubscription(subscription.getId());
        notify(subscription, "Subscription renewed", "Your subscription renewed automatically until "
            + next.get().atZone(java.time.ZoneOffset.UTC).toLocalDate() + ". A renewal invoice is in Billing.");
        auditService.recordSuccess("SUBSCRIPTION_AUTO_RENEWED", null, subscription.getOwnerCustomerId(), null,
            "ProductSubscription", subscription.getId().toString(), subscription.getOwnerOrganizationId(),
            "Auto-renewed from " + oldRenewal + " until " + next.get());
        publish(PlatformEventTypes.SUBSCRIPTION_RENEWED, subscription);
        return true;
    }

    /** One calendar period, read off the subscription's own plan — a
     * ONE_TIME plan (or no plan at all) has nothing to extend. */
    private Optional<Instant> nextExpiry(ProductSubscription subscription, Instant from) {
        BillingPeriod period = subscription.getPlanId() == null ? null
            : productPlanRepository.findById(subscription.getPlanId()).map(ProductPlan::getBillingPeriod).orElse(null);
        if (period == BillingPeriod.MONTHLY) {
            return Optional.of(from.plus(30, ChronoUnit.DAYS));
        }
        if (period == BillingPeriod.YEARLY) {
            return Optional.of(from.plus(365, ChronoUnit.DAYS));
        }
        // No plan, or a ONE_TIME plan: nothing recurring to renew, unless the
        // subscription already carries an expiry of its own (set some other
        // way) — extend that by the same 30-day default a monthly plan uses.
        return subscription.getExpiresAt() == null ? Optional.empty() : Optional.of(from.plus(30, ChronoUnit.DAYS));
    }

    /**
     * 07.04.01 Process renewal (sprint 2026.4.3): no payment integration
     * exists (see this class's own doc), so this simply extends the term —
     * there is nothing to charge yet. Refused when there is nothing to
     * renew (no plan and no prior expiry — see {@link #nextExpiry}).
     * Reactivates an EXPIRED subscription; leaves any other status alone.
     */
    @Transactional
    public SubscriptionDto renewSubscription(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("A cancelled subscription cannot be renewed.");
        }
        Instant from = subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(Instant.now())
            ? subscription.getExpiresAt() : Instant.now();
        Instant newExpiry = nextExpiry(subscription, from)
            .orElseThrow(() -> new IllegalArgumentException("This subscription has no billing period to renew."));
        subscription.setExpiresAt(newExpiry);
        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        subscription = subscriptionRepository.save(subscription);
        notify(subscription, "Subscription renewed", "Your subscription has been renewed.");
        auditService.recordSuccess("SUBSCRIPTION_RENEWED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null, "Subscription renewed until " + newExpiry);
        publish(PlatformEventTypes.SUBSCRIPTION_RENEWED, subscription);
        invoiceService.generateForSubscription(subscription.getId());
        return toDto(subscription);
    }

    /** 07.04.01 Process expiry (sprint 2026.4.3): called by
     * {@code SubscriptionExpiryJob} on a fixed schedule — flips every ACTIVE
     * subscription whose {@code expiresAt} has passed to EXPIRED. Skips
     * SUSPENDED/CANCELLED rows: those are already out of ACTIVE, so this
     * query never sees them. */
    @Transactional
    public int expireOverdueSubscriptions() {
        // REQ-SUB-004 (C64): auto-renewing subscriptions are renewed by
        // RenewalJob instead of expiring.
        List<ProductSubscription> overdue = subscriptionRepository
            .findByStatusAndAutoRenewFalseAndExpiresAtBefore(SubscriptionStatus.ACTIVE, Instant.now());
        for (ProductSubscription subscription : overdue) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            notify(subscription, "Subscription expired", "Your subscription has expired.");
            auditService.recordSuccess("SUBSCRIPTION_EXPIRED", null, subscription.getOwnerCustomerId(), null,
                "ProductSubscription", subscription.getId().toString(), null, "Subscription expired automatically");
        }
        return overdue.size();
    }

    /** 07.01.02 Change plan (upgrade/downgrade unified, sprint 2026.4.3) —
     * refused if the plan belongs to a different product, or the
     * subscription is cancelled. */
    @Transactional
    public SubscriptionDto changePlan(Long customerId, Long subscriptionId, ChangePlanRequest request) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("A cancelled subscription's plan cannot be changed.");
        }
        if (request.planId() != null) {
            ProductPlan plan = productPlanRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.planId()));
            if (!plan.getProduct().getId().equals(subscription.getProductId())) {
                throw new IllegalArgumentException("That plan does not belong to this subscription's product.");
            }
        }
        subscription.setPlanId(request.planId());
        subscription = subscriptionRepository.save(subscription);
        auditService.recordSuccess("SUBSCRIPTION_PLAN_CHANGED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null, "Plan changed to " + request.planId());
        publish(PlatformEventTypes.SUBSCRIPTION_CHANGED, subscription);
        return toDto(subscription);
    }
}
