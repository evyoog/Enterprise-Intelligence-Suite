package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByOwnerCustomerIdOrderByIssuedAtDesc(Long ownerCustomerId, Pageable pageable);
    Page<Invoice> findByOwnerOrganizationIdOrderByIssuedAtDesc(Long ownerOrganizationId, Pageable pageable);
    Page<Invoice> findAllByOrderByIssuedAtDesc(Pageable pageable);
    List<Invoice> findByOwnerCustomerIdAndStatus(Long ownerCustomerId, InvoiceStatus status);
    List<Invoice> findByOwnerOrganizationIdAndStatus(Long ownerOrganizationId, InvoiceStatus status);
    List<Invoice> findByOwnerCustomerIdAndStatusIn(Long ownerCustomerId, List<InvoiceStatus> statuses);
    List<Invoice> findByOwnerOrganizationIdAndStatusIn(Long ownerOrganizationId, List<InvoiceStatus> statuses);

    /** Platform admin dashboard (C53): every owner's paid/partially-refunded
     * invoices, filtered to a date window in Java by the caller — same style
     * as {@link com.vyoog.eisplatform.modules.billing.service.InvoiceService#spentInPeriod},
     * just without a single owner to scope the query by. */
    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);

    /** Demo data seeding (C54, {@code DemoDataSeeder}) only: backdates a
     * freshly-generated invoice's issue/due date into a past period, by a
     * bulk update that bypasses the {@code @CreatedDate} auditing listener
     * (which would otherwise overwrite any value {@code setIssuedAt} is
     * given before the first save with "now"). Never used by real invoice
     * generation — {@code InvoiceService#generateForSubscription} always
     * issues at the real current time. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Invoice i set i.issuedAt = :issuedAt, i.dueAt = :issuedAt where i.id = :id")
    void backdateIssuedAtForDemoData(@Param("id") Long id, @Param("issuedAt") Instant issuedAt);
}
