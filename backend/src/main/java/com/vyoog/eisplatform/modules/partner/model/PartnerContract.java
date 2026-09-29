package com.vyoog.eisplatform.modules.partner.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 14.01.02 Contracts (sprint 2027.2.1). One contract per provider — "Create
 * contract" and "Manage terms" are the same upsert (see
 * {@code PartnerService#createOrUpdateContract}), same fold pattern as
 * reviews' submit/edit. "Track expiration" is the scheduled job flipping
 * {@code status} once {@code endDate} has passed, not a read-time
 * computation.
 */
@Entity
@Table(name = "partner_contract")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class PartnerContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false, unique = true)
    private Long providerId;

    @Column(name = "terms", length = 4000, nullable = false)
    private String terms;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractStatus status = ContractStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
