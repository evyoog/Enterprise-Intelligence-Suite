package com.vyoog.eisplatform.modules.billing.tax.repository;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxClassRepository extends JpaRepository<TaxClass, Long> {
    Optional<TaxClass> findByCode(String code);
    boolean existsByCode(String code);
}
