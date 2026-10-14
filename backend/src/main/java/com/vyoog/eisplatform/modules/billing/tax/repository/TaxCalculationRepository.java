package com.vyoog.eisplatform.modules.billing.tax.repository;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {
    List<TaxCalculation> findByInvoiceIdOrderByIdDesc(Long invoiceId);
    Optional<TaxCalculation> findFirstByOrderByIdDesc();
    long countByInvoiceId(Long invoiceId);
}
