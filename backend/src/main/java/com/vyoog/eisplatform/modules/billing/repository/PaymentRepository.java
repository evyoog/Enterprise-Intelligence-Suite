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
    Optional<Payment> findByProviderAndProviderOrderId(String provider, String providerOrderId);
    Optional<Payment> findFirstByProviderAndProviderPaymentId(String provider, String providerPaymentId);
    boolean existsByProviderAndStatus(String provider, com.vyoog.eisplatform.modules.billing.model.PaymentStatus status);
    boolean existsByProviderAndRefundedAmountGreaterThan(String provider, long refundedAmount);
    Page<Payment> findByInvoiceIdIn(List<Long> invoiceIds, Pageable pageable);
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
