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
}
