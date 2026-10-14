package com.vyoog.eisplatform.modules.billing.tax.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C96 (REQ-BIL-002.1 to .6): the one row (id = 1) holding the External Tax Engine switch, the provider settings and the
 * last validation result. Provider credentials (base URL, API key, secret) are never stored here (BR-30, BR-SEC-001).
 * {@code configVersion} goes up whenever the engine or the provider settings change, so an invoice can record exactly
 * which configuration produced its tax (BR-2). */
@Entity
@Table(name = "tax_settings")
@Getter
@Setter
public class TaxSettings {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "external_engine_enabled", nullable = false)
    private boolean externalEngineEnabled = false;

    @Column(name = "config_version", nullable = false)
    private int configVersion = 1;

    @Column(name = "allow_self_approval", nullable = false)
    private boolean allowSelfApproval = false;

    @Column(name = "provider_code", length = 40)
    private String providerCode;

    @Column(name = "provider_company_code", length = 100)
    private String providerCompanyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_environment", nullable = false, length = 12)
    private ProviderEnvironment providerEnvironment = ProviderEnvironment.SANDBOX;

    @Column(name = "provider_timeout_seconds", nullable = false)
    private int providerTimeoutSeconds = 10;

    @Column(name = "provider_max_attempts", nullable = false)
    private int providerMaxAttempts = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 15)
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validation_summary", length = 2000)
    private String validationSummary;

    /** Comma-separated ISO country codes, currencies and transaction types the provider reported. */
    @Column(name = "provider_countries", length = 2000)
    private String providerCountries;

    @Column(name = "provider_currencies", length = 500)
    private String providerCurrencies;

    @Column(name = "provider_transaction_types", length = 200)
    private String providerTransactionTypes;

    @Column(name = "mode_changed_at")
    private Instant modeChangedAt;

    @Column(name = "mode_changed_by", length = 100)
    private String modeChangedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
