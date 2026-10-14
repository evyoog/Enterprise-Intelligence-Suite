package com.vyoog.eisplatform.modules.billing.tax.repository;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxRegistrationRepository extends JpaRepository<TaxRegistration, Long> {
    List<TaxRegistration> findAllByOrderByCountryCodeAscIdAsc();
}
