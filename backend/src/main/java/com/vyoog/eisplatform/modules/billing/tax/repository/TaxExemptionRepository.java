package com.vyoog.eisplatform.modules.billing.tax.repository;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxExemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxExemptionRepository extends JpaRepository<TaxExemption, Long> {
    List<TaxExemption> findAllByOrderByIdDesc();
    List<TaxExemption> findByOwnerCustomerIdAndCountryCode(Long ownerCustomerId, String countryCode);
    List<TaxExemption> findByOwnerOrganizationIdAndCountryCode(Long ownerOrganizationId, String countryCode);
}
