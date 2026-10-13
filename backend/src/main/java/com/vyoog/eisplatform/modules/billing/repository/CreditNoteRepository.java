package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    List<CreditNote> findByInvoiceIdOrderByIdAsc(Long invoiceId);

    @Query("select coalesce(sum(c.total), 0) from CreditNote c where c.invoiceId = :invoiceId")
    long totalCredited(@Param("invoiceId") Long invoiceId);

    @Query("select coalesce(sum(c.subtotal), 0) from CreditNote c where c.invoiceId = :invoiceId")
    long subtotalCredited(@Param("invoiceId") Long invoiceId);
}
