package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.InvoiceTaxLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceTaxLineRepository extends JpaRepository<InvoiceTaxLine, Long> {
    List<InvoiceTaxLine> findByInvoiceIdOrderBySortOrderAscIdAsc(Long invoiceId);

    @Modifying
    @Query("delete from InvoiceTaxLine l where l.invoiceId = :invoiceId")
    void deleteByInvoiceId(@Param("invoiceId") Long invoiceId);
}
