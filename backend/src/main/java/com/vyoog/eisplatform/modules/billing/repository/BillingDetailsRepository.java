package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingDetailsRepository extends JpaRepository<BillingDetails, Long> {
    Optional<BillingDetails> findByOwnerCustomerId(Long ownerCustomerId);
    Optional<BillingDetails> findByOwnerOrganizationId(Long ownerOrganizationId);
}
