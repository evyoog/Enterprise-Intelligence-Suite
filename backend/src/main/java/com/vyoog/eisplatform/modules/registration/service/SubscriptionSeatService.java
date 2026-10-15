package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.registration.dto.SeatSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REQ-SUB-003 (C63, C100) and REQ-TEN-005 (C90): seats of an organization's subscriptions. Seats in use are named per product:
 * the ACTIVE members who have that product ON; a change is never below the seats in use. An increase takes effect at once and is
 * charged for the rest of the term; a decrease takes effect with the next renewal, with no credit (C100 SE-1 B).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionSeatService {

    public static final int MAX_SEATS = 100_000;

    private final OrganizationSelfService organizationSelfService;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final OrganizationRepository organizationRepository;
    private final ProductSeatService productSeatService;
    private final SubscriptionService subscriptionService;
    private final InvoiceService invoiceService;
    private final ProductPlanRepository productPlanRepository;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    /** C100 SE-2 A: seats are seen and changed by anyone with Manage billing; Manage subscriptions can still see them. */
    private Long requireSeatViewer(Long customerId) {
        var caller = organizationSelfService.requireCaller(customerId);
        if (organizationSelfService.holdsOrganizationPermission(customerId, "MANAGE_ORGANIZATION_BILLING")
                || organizationSelfService.holdsOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS")) {
            return caller.getOrganizationId();
        }
        throw new com.vyoog.eisplatform.common.exception.ForbiddenException("You do not have permission to do this");
    }

    public List<SubscriptionDto> listOrganizationSubscriptions(Long customerId) {
        Long organizationId = requireSeatViewer(customerId);
        return subscriptionService.listOrganizationSubscriptions(organizationId);
    }

    public SeatSummaryDto summary(Long customerId, Long subscriptionId) {
        Long organizationId = requireSeatViewer(customerId);
        return toSummary(resolve(organizationId, subscriptionId), organizationId, null, null);
    }

    /**
     * C100 SE-1 B: an increase takes effect at once and is charged now for the remaining days of the term (its own invoice); a decrease takes
     * effect with the next renewal, with no credit, and can be withdrawn by asking for the current quantity again. Never below the seats in use.
     */
    @Transactional
    public SeatSummaryDto changeSeats(Long customerId, Long subscriptionId, int quantity) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_ORGANIZATION_BILLING");
        ProductSubscription subscription = resolve(organizationId, subscriptionId);
        if (quantity < 1 || quantity > MAX_SEATS) {
            throw new IllegalArgumentException("Seats must be between 1 and " + MAX_SEATS + ".");
        }
        if (subscription.isTrial()) {
            throw new IllegalArgumentException("A free trial has the organization's seats. Subscribe to change them.");
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new InvalidStateException("Seats can only be changed on an active or suspended subscription.");
        }
        Instant now = Instant.now();
        if (subscription.getExpiresAt() != null && !subscription.getExpiresAt().isAfter(now)) {
            throw new InvalidStateException("This term has ended. Renew the subscription before changing seats.");
        }
        long inUse = inUse(organizationId, subscription.getProductId());
        if (quantity < inUse) {
            throw new InvalidStateException(inUse + " seats are in use. Remove members before reducing seats.");
        }
        int from = subscription.getQuantity();
        if (quantity == from) {
            boolean withdrawn = subscription.getPendingQuantity() != null;
            if (withdrawn) {
                subscription.setPendingQuantity(null);
                subscriptionRepository.save(subscription);
                auditService.recordSuccess("SUBSCRIPTION_SEATS_DECREASE_WITHDRAWN", null, customerId, null, "ProductSubscription",
                    subscription.getId().toString(), organizationId, "Scheduled seat decrease withdrawn");
            }
            return toSummary(subscription, organizationId, withdrawn ? "WITHDRAWN" : "UNCHANGED", null);
        }
        if (quantity < from) {
            if (subscription.getExpiresAt() == null) {
                // No renewal date to wait for (no recurring plan): there is nothing to credit or schedule, so the decrease applies now.
                subscription.setQuantity(quantity);
                subscription.setPendingQuantity(null);
                subscriptionRepository.save(subscription);
                recordChange(subscription, organizationId, customerId, from, quantity, false);
                return toSummary(subscription, organizationId, "NOW", null);
            }
            subscription.setPendingQuantity(quantity);
            subscriptionRepository.save(subscription);
            auditService.recordSuccess("SUBSCRIPTION_SEATS_DECREASE_SCHEDULED", null, customerId, null, "ProductSubscription",
                subscription.getId().toString(), organizationId, "Seats will change from " + from + " to " + quantity + " at the next renewal");
            return toSummary(subscription, organizationId, "AT_RENEWAL", null);
        }

        // An increase: effective now, charged now for the rest of the term.
        subscription.setQuantity(quantity);
        subscription.setPendingQuantity(null);
        subscriptionRepository.save(subscription);

        // BR-5: an increase above the organization's seat limit raises it.
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (quantity > organization.getLicensedSeats()) {
            organization.setLicensedSeats(quantity);
            organizationRepository.save(organization);
        }
        Invoice invoice = invoiceService.generateSeatIncrease(subscription.getId(), quantity - from, remainingDays(subscription, now), termDays(subscription));
        recordChange(subscription, organizationId, customerId, from, quantity, invoice != null);
        // REQ-TEN-005 (AM-3 B): more seats let members who were left without one get their default-ON product.
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(organizationId));
        return toSummary(subscription, organizationId, "NOW", invoice == null ? null : invoice.getId());
    }

    private void recordChange(ProductSubscription subscription, Long organizationId, Long customerId, int from, int quantity, boolean charged) {
        auditService.recordSuccess("SUBSCRIPTION_SEATS_CHANGED", null, customerId, null, "ProductSubscription",
            subscription.getId().toString(), organizationId, "Seats changed from " + from + " to " + quantity + (charged ? " (charged for the rest of the term)" : ""));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getId());
        payload.put("organizationId", organizationId);
        payload.put("productId", subscription.getProductId());
        payload.put("fromQuantity", from);
        payload.put("toQuantity", quantity);
        payload.put("changedByCustomerId", customerId);
        outboxService.publish(PlatformEventTypes.SEATS_CHANGED, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, subscription.getId(), payload);
    }

    /** Days in the subscription's term: 30 (Monthly), 365 (Yearly), else the length of its current term. */
    int termDays(ProductSubscription subscription) {
        com.vyoog.eisplatform.modules.product.model.BillingPeriod period = subscription.getPlanId() == null ? null
            : productPlanRepository.findById(subscription.getPlanId()).map(com.vyoog.eisplatform.modules.product.model.ProductPlan::getBillingPeriod).orElse(null);
        if (period == com.vyoog.eisplatform.modules.product.model.BillingPeriod.YEARLY) {
            return 365;
        }
        if (period == com.vyoog.eisplatform.modules.product.model.BillingPeriod.MONTHLY) {
            return 30;
        }
        if (subscription.getStartedAt() != null && subscription.getExpiresAt() != null) {
            return (int) Math.max(1, Duration.between(subscription.getStartedAt(), subscription.getExpiresAt()).toDays());
        }
        return 30;
    }

    /** Days left in the term, rounded up so a part day counts as a day; 0 when there is no end date or it has passed. */
    int remainingDays(ProductSubscription subscription, Instant now) {
        if (subscription.getExpiresAt() == null || !subscription.getExpiresAt().isAfter(now)) {
            return 0;
        }
        long seconds = Duration.between(now, subscription.getExpiresAt()).getSeconds();
        return (int) Math.min(termDays(subscription), (seconds + 86_399) / 86_400);
    }

    private ProductSubscription resolve(Long organizationId, Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
            .filter(s -> s.getOwnerType() == RegistrationOwnerType.ORGANIZATION && organizationId.equals(s.getOwnerOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    /** AM-3 B (C90): named seats — the members who have this product ON. */
    private long inUse(Long organizationId, Long productId) {
        return productSeatService.seatsInUse(organizationId, productId);
    }

    private SeatSummaryDto toSummary(ProductSubscription subscription, Long organizationId, String effect, Long invoiceId) {
        long inUse = inUse(organizationId, subscription.getProductId());
        int seatLimit = organizationRepository.findById(organizationId).map(Organization::getLicensedSeats).orElse(0);
        String productName = subscriptionService.listOrganizationSubscriptions(organizationId).stream()
            .filter(s -> s.id().equals(subscription.getId())).map(SubscriptionDto::productName).findFirst().orElse(null);
        InvoiceService.SeatPrice price = invoiceService.seatPrice(subscription).orElse(null);
        return new SeatSummaryDto(subscription.getId(), productName, subscription.getQuantity(), inUse,
            Math.max(1, inUse), seatLimit, MAX_SEATS, subscription.getPendingQuantity(),
            price == null ? 0 : price.unitAmount(), price == null ? null : price.currency().name(),
            remainingDays(subscription, Instant.now()), termDays(subscription), subscription.getExpiresAt(), effect, invoiceId);
    }

    // REQ-TEN-005.4 (D16): suspend, reactivate, cancel and change plan need Manage subscriptions.

    @Transactional
    public SubscriptionDto suspend(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.suspendOrganizationSubscription(organizationId, customerId, subscriptionId);
    }

    @Transactional
    public SubscriptionDto reactivate(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.reactivateOrganizationSubscription(organizationId, customerId, subscriptionId);
    }

    @Transactional
    public SubscriptionDto cancel(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.cancelOrganizationSubscription(organizationId, customerId, subscriptionId);
    }

    @Transactional
    public SubscriptionDto changePlan(Long customerId, Long subscriptionId, com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest request) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.changeOrganizationPlan(organizationId, customerId, subscriptionId, request);
    }

    @Transactional
    public SubscriptionDto setAutoRenew(Long customerId, Long subscriptionId, boolean autoRenew) {
        Long organizationId = requireSeatViewer(customerId);
        return subscriptionService.setOrganizationAutoRenew(organizationId, customerId, subscriptionId, autoRenew);
    }

    @Transactional
    public SubscriptionDto cancelScheduledPlanChange(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.cancelScheduledOrganizationPlanChange(organizationId, customerId, subscriptionId);
    }
}
