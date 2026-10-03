package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.registration.dto.SeatSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REQ-SUB-003 (C63): seats of an organization's subscriptions. Seats in use
 * are the organization's ACTIVE members (a pool shared by its subscriptions);
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
    private final OrganizationMemberRepository memberRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;
    private final OutboxService outboxService;

    public List<SubscriptionDto> listOrganizationSubscriptions(Long customerId) {
        Long organizationId = organizationSelfService.requireOrganizationManagement(customerId);
        return subscriptionService.listOrganizationSubscriptions(organizationId);
    }

    public SeatSummaryDto summary(Long customerId, Long subscriptionId) {
        Long organizationId = organizationSelfService.requireOrganizationManagement(customerId);
        return toSummary(resolve(organizationId, subscriptionId), organizationId);
    }

    @Transactional
    public SeatSummaryDto changeSeats(Long customerId, Long subscriptionId, int quantity) {
        Long organizationId = organizationSelfService.requireOrganizationManagement(customerId);
        ProductSubscription subscription = resolve(organizationId, subscriptionId);
        if (quantity < 1 || quantity > MAX_SEATS) {
            throw new IllegalArgumentException("Seats must be between 1 and " + MAX_SEATS + ".");
        }
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            throw new InvalidStateException("Seats can only be changed on an active or suspended subscription.");
        }
        long inUse = inUse(organizationId);
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
        return toSummary(subscription, organizationId);
    }

    private ProductSubscription resolve(Long organizationId, Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
            .filter(s -> s.getOwnerType() == RegistrationOwnerType.ORGANIZATION && organizationId.equals(s.getOwnerOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    private long inUse(Long organizationId) {
        return memberRepository.countByOrganizationIdAndStatus(organizationId, MembershipStatus.ACTIVE);
    }

    private SeatSummaryDto toSummary(ProductSubscription subscription, Long organizationId) {
        long inUse = inUse(organizationId);
        int seatLimit = organizationRepository.findById(organizationId).map(Organization::getLicensedSeats).orElse(0);
        String productName = subscriptionService.listOrganizationSubscriptions(organizationId).stream()
            .filter(s -> s.id().equals(subscription.getId())).map(SubscriptionDto::productName).findFirst().orElse(null);
        return new SeatSummaryDto(subscription.getId(), productName, subscription.getQuantity(), inUse,
            Math.max(1, inUse), seatLimit, MAX_SEATS);
    }
}
