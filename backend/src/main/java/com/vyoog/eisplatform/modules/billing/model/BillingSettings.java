package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** C55 (REQ-BIL-001.21): one platform-wide row of offline bank details,
 * printed on offline invoices. Not secrets — never in the secrets file. */
@Entity
@Table(name = "billing_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BillingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offline_account_name", length = 200)
    private String offlineAccountName;

    @Column(name = "offline_bank_name", length = 200)
    private String offlineBankName;

    @Column(name = "offline_account_number", length = 34)
    private String offlineAccountNumber;

    @Column(name = "offline_ifsc", length = 11)
    private String offlineIfsc;

    @Column(name = "offline_swift_bic", length = 11)
    private String offlineSwiftBic;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by_customer_id")
    private Long updatedByCustomerId;
}
