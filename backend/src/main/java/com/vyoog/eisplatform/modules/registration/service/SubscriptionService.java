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
    /** C100 SE-1 B: seats in use, when a scheduled seat decrease is applied at renewal. */
    private final ProductSeatService productSeatService;
    /** REQ-CAT-005 (C85): per-product audience rule. */
    private final com.vyoog.eisplatform.modules.offering.service.EligibilityService eligibilityService;
    /** REQ-TEN-005 (C90): organization entitlement changes recalculate member product access. */
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    /** C101 (SR-1 A): the subscription's service instance follows its status. */
    private final com.vyoog.eisplatform.modules.serviceinstance.service.ServiceInstanceService serviceInstanceService;

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
        serviceInstanceService.syncFromSubscription(subscription);
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
        eligibilityService.assertEligible(productId, false);

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
        eligibilityService.assertEligible(productId, false);

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerCustomerIdAndProductId(customerId, productId)
            .orElseGet(() -> {
                ProductSubscription created = new ProductSubscription();
                created.setProductId(productId);
                created.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
                created.setOwnerCustomerId(customerId);
                return created;
            });
        // C97 BI-1: a paid purchase starts after payment (card or UPI) or at once with a due date (pay by invoice). Until then it is
        // PENDING_SUBSCRIPTION. A subscription that is already active keeps running while the new purchase is paid.
        boolean paid = planId != null && productPlanRepository.findById(planId).map(p -> p.getPrice().signum() > 0).orElse(false);
        boolean alreadyActive = subscription.getStatus() == SubscriptionStatus.ACTIVE;
        boolean startNow = !paid || alreadyActive;
        subscription.setStatus(startNow ? SubscriptionStatus.ACTIVE : SubscriptionStatus.PENDING_SUBSCRIPTION);
        subscription.setStartedAt(Instant.now());
        subscription.setPlanId(planId);
        // REQ-SUB-004.1 (C64): a Monthly/Yearly plan gets a renewal date and auto-renew.
        subscription.setExpiresAt(firstRenewalDate(planId, subscription.getStartedAt()));
        subscription.setAutoRenew(true);
        subscription = subscriptionRepository.save(subscription);

        if (startNow) {
            customerRepository.findById(customerId).ifPresent(customer ->
                notificationService.notify(customerId, customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO,
                    "Subscribed to " + product.getName(),
                    "You're now subscribed to " + product.getName() + "."));
            auditService.recordSuccess("SUBSCRIPTION_CREATED", null, customerId, null,
                "ProductSubscription", productId.toString(), null, "Customer subscribed to product " + productId + " from the cart");
            publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        } else {
            customerRepository.findById(customerId).ifPresent(customer ->
                notificationService.notify(customerId, customer.getEmail(), NotificationCategory.SUBSCRIPTION, NotificationSeverity.INFO,
                    "Order received: " + product.getName(),
                    "Your subscription to " + product.getName() + " starts when your payment is received."));
            auditService.recordSuccess("SUBSCRIPTION_ORDERED", null, customerId, null,
                "ProductSubscription", productId.toString(), null, "Customer ordered product " + productId + " from the cart; it starts after payment");
        }
        return subscription.getId();
    }

    /** C97 BI-1: starts a purchase that was waiting for payment. Does nothing for a subscription that is not PENDING_SUBSCRIPTION, so a
     * second payment event for the same invoice changes nothing. The term starts now. */
    @Transactional
    public boolean activatePending(Long subscriptionId, String reason) {
        ProductSubscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null || subscription.getStatus() != SubscriptionStatus.PENDING_SUBSCRIPTION) {
            return false;
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(firstRenewalDate(subscription.getPlanId(), subscription.getStartedAt()));
        subscription = subscriptionRepository.save(subscription);
        String productName = productRepository.findById(subscription.getProductId()).map(Product::getName).orElse("your product");
        notify(subscription, "Subscription active", "Your subscription to " + productName + " is now active.");
        auditService.recordSuccess("SUBSCRIPTION_ACTIVATED", null, subscription.getOwnerCustomerId(), null,
            "ProductSubscription", subscription.getId().toString(), subscription.getOwnerOrganizationId(), "Subscription started: " + reason);
        publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        if (subscription.getOwnerOrganizationId() != null) {
            eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(subscription.getOwnerOrganizationId()));
        }
        return true;
    }

    /** 09.02.01 Service Provisioning (sprint 2027.1.1): the organization
     * equivalent of {@link #subscribe} — called by {@code OrderService} once
     * an order is approved, never directly by a member (there is no
     * self-serve organization-subscribe endpoint; every org subscription
     * goes through an order). Unlike {@code subscribe}, this accepts a plan
     * up front, since the order that triggers it already carries one. */
    @Transactional
    public SubscriptionDto subscribeOrganization(Long organizationId, Long productId, Long planId) {
        ProductSubscription subscription = activateOrganizationSubscription(organizationId, productId, planId);
        invoiceService.generateForSubscription(subscription.getId());
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(organizationId));
        return toDto(subscription, productRepository.findById(productId).map(Product::getName).orElse("Unknown product"));
    }

    /** C100 CA-1 B: an approved or direct organization purchase of several products: every subscription is created and ONE invoice bills them all (with the
     * coupon, if any). Returns the invoice id, or null when everything was free. */
    @Transactional
    public Long subscribeOrganizationItems(Long organizationId, List<long[]> productAndPlan, String couponCode) {
        List<Long> subscriptionIds = new java.util.ArrayList<>();
        for (long[] item : productAndPlan) {
            Long planId = item[1] == 0 ? null : item[1];
            subscriptionIds.add(activateOrganizationSubscription(organizationId, item[0], planId).getId());
        }
        var invoice = invoiceService.generateForSubscriptions(subscriptionIds, couponCode);
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(organizationId));
        return invoice == null ? null : invoice.getId();
    }

    /** C100 SE-1 B: the seats a purchase of this product starts with (and is billed for): the existing subscription's seats, else, as REQ-SUB-003's
     * engineering default, the organization's licensed seats (at least the active members, at least 1). */
    public int initialSeats(Long organizationId, Long productId) {
        var existing = subscriptionRepository.findByOwnerOrganizationIdAndProductId(organizationId, productId);
        if (existing.isPresent() && existing.get().getQuantity() > 0) {
            return existing.get().getQuantity();
        }
        int licensed = organizationRepository.findById(organizationId).map(o -> o.getLicensedSeats()).orElse(0);
        long active = memberRepository.countByOrganizationIdAndStatus(organizationId,
            com.vyoog.eisplatform.modules.registration.model.MembershipStatus.ACTIVE);
        return (int) Math.min(SubscriptionSeatService.MAX_SEATS, Math.max(1, Math.max(licensed, active)));
    }

    /** Creates or reactivates an organization's subscription to a product on a plan, with its audit and event; the caller bills it. */
    private ProductSubscription activateOrganizationSubscription(Long organizationId, Long productId, Long planId) {
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
            subscription.setQuantity(initialSeats(organizationId, productId));
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setPlanId(planId);
        // REQ-SUB-004.1 (C64): a Monthly/Yearly plan gets a renewal date and auto-renew.
        subscription.setExpiresAt(firstRenewalDate(planId, subscription.getStartedAt()));
        subscription.setAutoRenew(true);
        subscription.setGraceEndsAt(null);
        subscription.setPendingQuantity(null);
        subscription = subscriptionRepository.save(subscription);

        auditService.recordSuccess("SUBSCRIPTION_CREATED", null, null, null,
            "ProductSubscription", productId.toString(), organizationId, "Organization subscribed to product " + productId);
        publish(PlatformEventTypes.SUBSCRIPTION_CREATED, subscription);
        return subscription;
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
            subscription.isAutoRenew(),
            subscription.getPendingPlanId(),
            subscription.getPendingPlanId() == null ? null
                : productPlanRepository.findById(subscription.getPendingPlanId()).map(ProductPlan::getName).orElse(null),
            subscription.getPendingQuantity(),
            subscription.getGraceEndsAt()
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
        applyPendingPlan(subscription);   // C97 SC-1: a scheduled downgrade takes effect with this renewal
        applyPendingSeats(subscription);  // C100 SE-1 B: so does a scheduled seat decrease
        Optional<Instant> next = nextExpiry(subscription, oldRenewal);
        if (next.isEmpty()) {
            return false;
        }
        subscription.setGraceEndsAt(null);   // C100 RR-1 B: renewing ends a grace period
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
        applyPendingPlan(subscription);   // C97 SC-1
        applyPendingSeats(subscription);  // C100 SE-1 B
        Instant newExpiry = nextExpiry(subscription, from)
            .orElseThrow(() -> new IllegalArgumentException("This subscription has no billing period to renew."));
        subscription.setGraceEndsAt(null);   // C100 RR-1 B
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

    /** C100 RR-1 B: how long a subscription stays read-only after its term ends with auto-renew off. */
    public static final int GRACE_DAYS = 7;

    /** 07.04.01 Process expiry (sprint 2026.4.3), C100 RR-1 B: called by {@code SubscriptionExpiryJob}. A subscription whose term has ended with
     * auto-renew OFF first gets a {@value #GRACE_DAYS}-day read-only grace period (it stays ACTIVE, with {@code graceEndsAt}); when that has passed it is
     * flipped to EXPIRED. Returns the number that expired. SUSPENDED and CANCELLED rows are never seen by this query. */
    @Transactional
    public int expireOverdueSubscriptions() {
        // REQ-SUB-004 (C64): auto-renewing subscriptions are renewed by RenewalJob instead.
        Instant now = Instant.now();
        List<ProductSubscription> overdue = subscriptionRepository
            .findByStatusAndAutoRenewFalseAndExpiresAtBefore(SubscriptionStatus.ACTIVE, now);
        int expired = 0;
        for (ProductSubscription subscription : overdue) {
            if (subscription.getGraceEndsAt() == null) {
                Instant graceEnd = subscription.getExpiresAt().plus(GRACE_DAYS, ChronoUnit.DAYS);
                subscription.setGraceEndsAt(graceEnd);
                subscriptionRepository.save(subscription);
                if (graceEnd.isAfter(now)) {
                    notify(subscription, "Subscription ended: read-only", "Your subscription has ended. It stays read-only until "
                        + graceEnd.atZone(java.time.ZoneOffset.UTC).toLocalDate() + "; renew before then to keep full access.");
                    auditService.recordSuccess("SUBSCRIPTION_GRACE_STARTED", null, subscription.getOwnerCustomerId(), null,
                        "ProductSubscription", subscription.getId().toString(), subscription.getOwnerOrganizationId(),
                        "Term ended with auto-renew off; read-only until " + graceEnd);
                    publish(PlatformEventTypes.SUBSCRIPTION_CHANGED, subscription);
                    continue;
                }
            } else if (subscription.getGraceEndsAt().isAfter(now)) {
                continue;
            }
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            notify(subscription, "Subscription expired", "Your subscription has expired.");
            auditService.recordSuccess("SUBSCRIPTION_EXPIRED", null, subscription.getOwnerCustomerId(), null,
                "ProductSubscription", subscription.getId().toString(), null, "Subscription expired automatically");
            if (subscription.getOwnerOrganizationId() != null) {
                eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(
                    subscription.getOwnerOrganizationId()));
            }
            expired++;
        }
        return expired;
    }

    /** C100 RR-1 B: the customer turns auto-renew on or off. Turning it on during a grace period renews at once. */
    @Transactional
    public SubscriptionDto setAutoRenew(Long customerId, Long subscriptionId, boolean autoRenew) {
        return changeAutoRenew(resolveOwnSubscription(customerId, subscriptionId), customerId, autoRenew);
    }

    @Transactional
    public SubscriptionDto setOrganizationAutoRenew(Long organizationId, Long actorCustomerId, Long subscriptionId, boolean autoRenew) {
        return changeAutoRenew(resolveOrganizationSubscription(organizationId, subscriptionId), actorCustomerId, autoRenew);
    }

    private SubscriptionDto changeAutoRenew(ProductSubscription subscription, Long actorCustomerId, boolean autoRenew) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new IllegalArgumentException("Auto-renew can only be changed on an active or suspended subscription.");
        }
        if (subscription.getExpiresAt() == null) {
            throw new IllegalArgumentException("This subscription has no renewal date.");
        }
        if (subscription.isAutoRenew() != autoRenew) {
            subscription.setAutoRenew(autoRenew);
            subscription = subscriptionRepository.save(subscription);
            auditService.recordSuccess("SUBSCRIPTION_AUTO_RENEW_CHANGED", null, actorCustomerId, null, "ProductSubscription",
                subscription.getId().toString(), subscription.getOwnerOrganizationId(), "Auto-renew turned " + (autoRenew ? "on" : "off"));
            publish(PlatformEventTypes.SUBSCRIPTION_CHANGED, subscription);
            if (autoRenew && subscription.getGraceEndsAt() != null && subscription.getStatus() == SubscriptionStatus.ACTIVE
                    && !subscription.getExpiresAt().isAfter(Instant.now())) {
                autoRenew(subscription.getId());
                subscription = subscriptionRepository.findById(subscription.getId()).orElse(subscription);
            }
        }
        return toDto(subscription);
    }

    /** 07.01.02 Change plan (sprint 2026.4.3), C97 SC-1: an upgrade (or a plan of the same price) takes effect at once; a downgrade is
     * scheduled and takes effect at the next renewal, so the customer keeps what they paid for until then. Refused if the plan belongs to
     * a different product, or the subscription is cancelled. */
    @Transactional
    public SubscriptionDto changePlan(Long customerId, Long subscriptionId, ChangePlanRequest request) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        boolean scheduled = applyPlanChange(subscription, request);
        auditService.recordSuccess(scheduled ? "SUBSCRIPTION_PLAN_CHANGE_SCHEDULED" : "SUBSCRIPTION_PLAN_CHANGED", null, customerId, null,
            "ProductSubscription", subscriptionId.toString(), null,
            (scheduled ? "Downgrade to plan " + request.planId() + " scheduled for the next renewal" : "Plan changed to " + request.planId()));
        if (scheduled) {
            notify(subscription, "Plan change scheduled", "Your plan will change at your next renewal on "
                + (subscription.getExpiresAt() == null ? "the renewal date" : subscription.getExpiresAt().atZone(java.time.ZoneOffset.UTC).toLocalDate()) + ".");
        }
        publish(PlatformEventTypes.SUBSCRIPTION_CHANGED, subscription);
        return toDto(subscription);
    }

    /** Validates and applies (or schedules) a plan change; returns true when it was scheduled rather than applied. */
    private boolean applyPlanChange(ProductSubscription subscription, ChangePlanRequest request) {
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("A cancelled subscription's plan cannot be changed.");
        }
        ProductPlan target = null;
        if (request.planId() != null) {
            target = productPlanRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.planId()));
            if (!target.getProduct().getId().equals(subscription.getProductId())) {
                throw new IllegalArgumentException("That plan does not belong to this subscription's product.");
            }
        }
        ProductPlan current = subscription.getPlanId() == null ? null : productPlanRepository.findById(subscription.getPlanId()).orElse(null);
        if (target != null && current != null && target.getId().equals(current.getId())) {
            // Choosing the plan already in force just withdraws a scheduled change.
            clearPendingPlan(subscription);
            subscriptionRepository.save(subscription);
            return false;
        }
        if (target != null && current != null && isDowngrade(current, target)) {
            subscription.setPendingPlanId(target.getId());
            subscription.setPendingPlanRequestedAt(Instant.now());
            subscriptionRepository.save(subscription);
            return true;
        }
        clearPendingPlan(subscription);
        subscription.setPlanId(request.planId());
        subscriptionRepository.save(subscription);
        return false;
    }

    /** A downgrade is a cheaper plan, compared per month (a yearly price is divided by 12; a one-time price is taken as it is). */
    static boolean isDowngrade(ProductPlan current, ProductPlan target) {
        return monthlyPrice(target).compareTo(monthlyPrice(current)) < 0;
    }

    private static java.math.BigDecimal monthlyPrice(ProductPlan plan) {
        java.math.BigDecimal price = plan.getPrice() == null ? java.math.BigDecimal.ZERO : plan.getPrice();
        return plan.getBillingPeriod() == BillingPeriod.YEARLY ? price.divide(java.math.BigDecimal.valueOf(12), 4, java.math.RoundingMode.HALF_UP) : price;
    }

    private static void clearPendingPlan(ProductSubscription subscription) {
        subscription.setPendingPlanId(null);
        subscription.setPendingPlanRequestedAt(null);
    }

    /** C97 SC-1: withdraws a scheduled downgrade. */
    @Transactional
    public SubscriptionDto cancelScheduledPlanChange(Long customerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOwnSubscription(customerId, subscriptionId);
        return withdrawScheduledChange(subscription, customerId);
    }

    @Transactional
    public SubscriptionDto cancelScheduledOrganizationPlanChange(Long organizationId, Long actorCustomerId, Long subscriptionId) {
        return withdrawScheduledChange(resolveOrganizationSubscription(organizationId, subscriptionId), actorCustomerId);
    }

    private SubscriptionDto withdrawScheduledChange(ProductSubscription subscription, Long actorCustomerId) {
        if (subscription.getPendingPlanId() != null) {
            clearPendingPlan(subscription);
            subscription = subscriptionRepository.save(subscription);
            auditService.recordSuccess("SUBSCRIPTION_PLAN_CHANGE_WITHDRAWN", null, actorCustomerId, null, "ProductSubscription",
                subscription.getId().toString(), subscription.getOwnerOrganizationId(), "Scheduled plan change withdrawn");
        }
        return toDto(subscription);
    }

    /** C100 SE-1 B: at a renewal, a scheduled seat decrease takes effect (never below the seats in use), so the renewal is billed for fewer seats. */
    private void applyPendingSeats(ProductSubscription subscription) {
        Integer pending = subscription.getPendingQuantity();
        if (pending == null) {
            return;
        }
        long inUse = subscription.getOwnerOrganizationId() == null ? 0
            : productSeatService.seatsInUse(subscription.getOwnerOrganizationId(), subscription.getProductId());
        int target = (int) Math.max(Math.max(1, pending), inUse);
        int from = subscription.getQuantity();
        subscription.setPendingQuantity(null);
        if (target != from) {
            subscription.setQuantity(target);
            auditService.recordSuccess("SUBSCRIPTION_SEATS_CHANGED", null, subscription.getOwnerCustomerId(), null, "ProductSubscription",
                subscription.getId().toString(), subscription.getOwnerOrganizationId(),
                "Seats changed from " + from + " to " + target + " at renewal" + (target != pending ? " (" + inUse + " seats are in use)" : ""));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("subscriptionId", subscription.getId());
            payload.put("organizationId", subscription.getOwnerOrganizationId());
            payload.put("productId", subscription.getProductId());
            payload.put("fromQuantity", from);
            payload.put("toQuantity", target);
            outboxService.publish(PlatformEventTypes.SEATS_CHANGED, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, subscription.getId(), payload);
        }
    }

    /** C97 SC-1: at a renewal, a scheduled downgrade takes effect, so the renewal is billed at the new plan's price. */
    private void applyPendingPlan(ProductSubscription subscription) {
        Long pending = subscription.getPendingPlanId();
        if (pending == null) {
            return;
        }
        if (productPlanRepository.existsById(pending)) {
            subscription.setPlanId(pending);
            auditService.recordSuccess("SUBSCRIPTION_PLAN_CHANGED", null, subscription.getOwnerCustomerId(), null, "ProductSubscription",
                subscription.getId().toString(), subscription.getOwnerOrganizationId(), "Scheduled downgrade to plan " + pending + " applied at renewal");
        }
        clearPendingPlan(subscription);
    }

    // ------------------------------------------------------------------
    // REQ-TEN-005.4 (D16, C65): organization subscription actions. The caller's permission (Manage subscriptions)
    // is checked by SubscriptionSeatService; these methods act on one subscription of the caller's organization.
    // ------------------------------------------------------------------

    private ProductSubscription resolveOrganizationSubscription(Long organizationId, Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
            .filter(s -> s.getOwnerType() == RegistrationOwnerType.ORGANIZATION && organizationId.equals(s.getOwnerOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    private void afterOrganizationChange(ProductSubscription subscription, Long actorCustomerId, String action, String title, String message, String eventType) {
        Long organizationId = subscription.getOwnerOrganizationId();
        auditService.recordSuccess(action, null, actorCustomerId, null,
            "ProductSubscription", subscription.getId().toString(), organizationId, message);
        publish(eventType, subscription);
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(organizationId));
        try {
            String product = productRepository.findById(subscription.getProductId()).map(Product::getName).orElse("a product");
            memberRepository.findByOrganizationIdAndOrgRoleAndStatus(organizationId,
                com.vyoog.eisplatform.modules.registration.model.OrgRole.ORG_ADMIN,
                com.vyoog.eisplatform.modules.registration.model.MembershipStatus.ACTIVE).forEach(admin ->
                customerRepository.findById(admin.getCustomerId()).ifPresent(customer ->
                    notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.SUBSCRIPTION,
                        NotificationSeverity.INFO, title + ": " + product, message + " (" + product + ")")));
        } catch (RuntimeException ignored) {
            // a failed notice never fails the change
        }
    }

    @Transactional
    public SubscriptionDto suspendOrganizationSubscription(Long organizationId, Long actorCustomerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOrganizationSubscription(organizationId, subscriptionId);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an active subscription can be suspended.");
        }
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription = subscriptionRepository.save(subscription);
        afterOrganizationChange(subscription, actorCustomerId, "SUBSCRIPTION_SUSPENDED", "Subscription suspended",
            "The organization's subscription was suspended", PlatformEventTypes.SUBSCRIPTION_SUSPENDED);
        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto reactivateOrganizationSubscription(Long organizationId, Long actorCustomerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOrganizationSubscription(organizationId, subscriptionId);
        if (subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new IllegalArgumentException("Only a suspended subscription can be reactivated.");
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);
        afterOrganizationChange(subscription, actorCustomerId, "SUBSCRIPTION_REACTIVATED", "Subscription reactivated",
            "The organization's subscription was reactivated", PlatformEventTypes.SUBSCRIPTION_RESUMED);
        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto cancelOrganizationSubscription(Long organizationId, Long actorCustomerId, Long subscriptionId) {
        ProductSubscription subscription = resolveOrganizationSubscription(organizationId, subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("This subscription is already cancelled.");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);
        afterOrganizationChange(subscription, actorCustomerId, "SUBSCRIPTION_CANCELLED", "Subscription cancelled",
            "The organization's subscription was cancelled", PlatformEventTypes.SUBSCRIPTION_CANCELLED);
        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto changeOrganizationPlan(Long organizationId, Long actorCustomerId, Long subscriptionId, ChangePlanRequest request) {
        ProductSubscription subscription = resolveOrganizationSubscription(organizationId, subscriptionId);
        boolean scheduled = applyPlanChange(subscription, request);
        afterOrganizationChange(subscription, actorCustomerId, scheduled ? "SUBSCRIPTION_PLAN_CHANGE_SCHEDULED" : "SUBSCRIPTION_PLAN_CHANGED",
            scheduled ? "Subscription plan change scheduled" : "Subscription plan changed",
            scheduled ? "The organization's plan will change at the next renewal" : "The organization's subscription plan was changed",
            PlatformEventTypes.SUBSCRIPTION_CHANGED);
        return toDto(subscription);
    }
}
