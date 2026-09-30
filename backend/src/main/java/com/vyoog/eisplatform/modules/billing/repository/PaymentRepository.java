package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceId(Long invoiceId);
    Optional<Payment> findByProviderOrderId(String providerOrderId);
    Page<Payment> findByInvoiceIdIn(List<Long> invoiceIds, Pageable pageable);
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
