package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSubscriptionRepository extends JpaRepository<ProductSubscription, Long> {

    List<ProductSubscription> findByOwnerCustomerId(Long ownerCustomerId);

    List<ProductSubscription> findByOwnerOrganizationId(Long ownerOrganizationId);

    Optional<ProductSubscription> findByOwnerCustomerIdAndProductId(Long ownerCustomerId, Long productId);

    Optional<ProductSubscription> findByOwnerOrganizationIdAndProductId(Long ownerOrganizationId, Long productId);

    boolean existsByProductId(Long productId);
}
