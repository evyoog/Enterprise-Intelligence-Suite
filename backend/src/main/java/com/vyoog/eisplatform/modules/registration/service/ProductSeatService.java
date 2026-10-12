package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * REQ-TEN-005 (AM-3 option B, C90): named seats per product. A member uses one seat of product X only while X is ON for
 * them; the seats of a product are the quantity of the organization's ACTIVE subscription to it. A platform administrator's
 * seat-overage setting on the organization lifts the limit. Replaces the shared pool of REQ-SUB-003.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSeatService {

    private final OrganizationProductAccessRepository accessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final OrganizationRepository organizationRepository;

    /** Seats in use on a product: ACTIVE access rows of ACTIVE members. */
    public long seatsInUse(Long organizationId, Long productId) {
        return accessRepository.countSeatsInUse(organizationId, productId, MembershipStatus.ACTIVE, MembershipStatus.ACTIVE);
    }

    /** The ACTIVE organization subscription for the product, if any. */
    public Optional<ProductSubscription> activeSubscription(Long organizationId, Long productId) {
        return subscriptionRepository.findByOwnerOrganizationId(organizationId).stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE && productId.equals(s.getProductId()))
            .findFirst();
    }

    public boolean overageAllowed(Long organizationId) {
        return organizationRepository.findById(organizationId).map(Organization::isAllowSeatOverage).orElse(false);
    }

    /** Free seats on the product now; {@link Long#MAX_VALUE} when overage is allowed; 0 without an active subscription. */
    public long freeSeats(Long organizationId, Long productId) {
        Optional<ProductSubscription> subscription = activeSubscription(organizationId, productId);
        if (subscription.isEmpty()) {
            return 0;
        }
        if (overageAllowed(organizationId)) {
            return Long.MAX_VALUE;
        }
        return Math.max(0, subscription.get().getQuantity() - seatsInUse(organizationId, productId));
    }

    /** BR-8: refuses with "No free seats on {product}". */
    public void assertSeatFree(Long organizationId, Long productId, String productName) {
        if (freeSeats(organizationId, productId) <= 0) {
            throw new SeatLimitExceededException("No free seats on " + productName
                + ". Add seats to the subscription, or turn the product off for another member first.");
        }
    }
}
