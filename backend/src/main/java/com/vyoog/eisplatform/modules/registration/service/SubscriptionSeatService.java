package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REQ-SUB-003 (C63) and REQ-TEN-005 (C90): seats of an organization's subscriptions. Seats in use are named per product:
 * the ACTIVE members who have that product ON;
 * a change takes effect at once and is never below the seats in use. No
 * charge or credit is made for a mid-term change (Open question 1).
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
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public List<SubscriptionDto> listOrganizationSubscriptions(Long customerId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.listOrganizationSubscriptions(organizationId);
    }

    public SeatSummaryDto summary(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return toSummary(resolve(organizationId, subscriptionId), organizationId);
    }

    @Transactional
    public SeatSummaryDto changeSeats(Long customerId, Long subscriptionId, int quantity) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        ProductSubscription subscription = resolve(organizationId, subscriptionId);
        if (quantity < 1 || quantity > MAX_SEATS) {
            throw new IllegalArgumentException("Seats must be between 1 and " + MAX_SEATS + ".");
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new InvalidStateException("Seats can only be changed on an active or suspended subscription.");
        }
        long inUse = inUse(organizationId, subscription.getProductId());
        if (quantity < inUse) {
            throw new InvalidStateException(inUse + " seats are in use. Remove members before reducing seats.");
        }
        int from = subscription.getQuantity();
        if (from == quantity) {
            return toSummary(subscription, organizationId);
        }
        subscription.setQuantity(quantity);
        subscriptionRepository.save(subscription);

        // BR-5: an increase above the organization's seat limit raises it.
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (quantity > organization.getLicensedSeats()) {
            organization.setLicensedSeats(quantity);
            organizationRepository.save(organization);
        }

        auditService.recordSuccess("SUBSCRIPTION_SEATS_CHANGED", null, customerId, null, "ProductSubscription",
            subscription.getId().toString(), organizationId, "Seats changed from " + from + " to " + quantity);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getId());
        payload.put("organizationId", organizationId);
        payload.put("productId", subscription.getProductId());
        payload.put("fromQuantity", from);
        payload.put("toQuantity", quantity);
        payload.put("changedByCustomerId", customerId);
        outboxService.publish(PlatformEventTypes.SEATS_CHANGED, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, subscription.getId(), payload);
        // REQ-TEN-005 (AM-3 B): more seats let members who were left without one get their default-ON product.
        eventPublisher.publishEvent(new com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent(organizationId));
        return toSummary(subscription, organizationId);
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

    private SeatSummaryDto toSummary(ProductSubscription subscription, Long organizationId) {
        long inUse = inUse(organizationId, subscription.getProductId());
        int seatLimit = organizationRepository.findById(organizationId).map(Organization::getLicensedSeats).orElse(0);
        String productName = subscriptionService.listOrganizationSubscriptions(organizationId).stream()
            .filter(s -> s.id().equals(subscription.getId())).map(SubscriptionDto::productName).findFirst().orElse(null);
        return new SeatSummaryDto(subscription.getId(), productName, subscription.getQuantity(), inUse,
            Math.max(1, inUse), seatLimit, MAX_SEATS);
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
    public SubscriptionDto cancelScheduledPlanChange(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customerId, "MANAGE_SUBSCRIPTIONS");
        return subscriptionService.cancelScheduledOrganizationPlanChange(organizationId, customerId, subscriptionId);
    }
}
