package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.InvoiceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceReminderRepository extends JpaRepository<InvoiceReminder, Long> {
    boolean existsByInvoiceIdAndKind(Long invoiceId, InvoiceReminder.Kind kind);

    List<InvoiceReminder> findByInvoiceIdOrderBySentAtAsc(Long invoiceId);
}
