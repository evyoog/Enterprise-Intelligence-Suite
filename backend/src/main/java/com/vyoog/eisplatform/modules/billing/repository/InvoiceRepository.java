package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByOwnerCustomerIdOrderByIssuedAtDesc(Long ownerCustomerId, Pageable pageable);
    Page<Invoice> findByOwnerOrganizationIdOrderByIssuedAtDesc(Long ownerOrganizationId, Pageable pageable);
    Page<Invoice> findAllByOrderByIssuedAtDesc(Pageable pageable);
    List<Invoice> findByOwnerCustomerIdAndStatus(Long ownerCustomerId, InvoiceStatus status);
    List<Invoice> findByOwnerOrganizationIdAndStatus(Long ownerOrganizationId, InvoiceStatus status);
    List<Invoice> findByOwnerCustomerIdAndStatusIn(Long ownerCustomerId, List<InvoiceStatus> statuses);
    List<Invoice> findByOwnerOrganizationIdAndStatusIn(Long ownerOrganizationId, List<InvoiceStatus> statuses);
}
