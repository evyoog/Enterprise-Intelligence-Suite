package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductSubscriptionRepository extends JpaRepository<ProductSubscription, Long> {

    List<ProductSubscription> findByOwnerCustomerId(Long ownerCustomerId);

    List<ProductSubscription> findByOwnerOrganizationId(Long ownerOrganizationId);

    Optional<ProductSubscription> findByOwnerCustomerIdAndProductId(Long ownerCustomerId, Long productId);

    Optional<ProductSubscription> findByOwnerOrganizationIdAndProductId(Long ownerOrganizationId, Long productId);

    boolean existsByProductId(Long productId);

    /** 07.04.01 Process expiry (sprint 2026.4.3): everything the nightly
     * {@code SubscriptionExpiryJob} needs to flip to EXPIRED. */
    List<ProductSubscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, Instant instant);

    /** REQ-SUB-004: the hourly expiry only expires subscriptions that do not auto-renew. */
    List<ProductSubscription> findByStatusAndAutoRenewFalseAndExpiresAtBefore(SubscriptionStatus status, Instant instant);

    /** REQ-SUB-004.2: auto-renewing subscriptions whose renewal date has come. */
    List<ProductSubscription> findByStatusAndAutoRenewTrueAndExpiresAtLessThanEqual(SubscriptionStatus status, Instant instant);

    /** REQ-SUB-004.6: subscriptions renewing within a window (reminder candidates). */
    List<ProductSubscription> findByStatusAndExpiresAtBetween(SubscriptionStatus status, Instant from, Instant to);

    /** Platform admin dashboard (C53): subscriptions by status, platform-wide. */
    long countByStatus(SubscriptionStatus status);
}
