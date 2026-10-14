package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C98 / REQ-BIL-003: one payment provider's configuration. Credentials are stored only as AES-GCM ciphertext
 * ({@link #credentialsEncrypted}); nothing here is ever returned by an API. Configured, connected (the last test), active and default are separate. */
@Entity
@Table(name = "payment_gateway_config")
@Getter
@Setter
public class PaymentGatewayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_code", nullable = false, unique = true, length = 30)
    private String providerCode;

    /** TEST or LIVE; null until chosen (a configuration that predates this screen). */
    @Column(length = 10)
    private String environment;

    @Column(nullable = false)
    private boolean activated = false;

    @Column(name = "default_gateway", nullable = false)
    private boolean defaultGateway = false;

    @Column(name = "credentials_encrypted", columnDefinition = "TEXT")
    private String credentialsEncrypted;

    @Column(name = "credentials_updated_at")
    private Instant credentialsUpdatedAt;

    /** Incremented by every change to credentials or environment; a test result counts only for the version it tested. */
    @Column(name = "config_version", nullable = false)
    private int configVersion = 1;

    /** Comma-separated methods switched on; null means every method the provider supports. */
    @Column(name = "enabled_methods", length = 100)
    private String enabledMethods;

    @Column(name = "test_status", length = 30)
    private String testStatus;

    @Column(name = "test_message", length = 300)
    private String testMessage;

    @Column(name = "tested_at")
    private Instant testedAt;

    @Column(name = "tested_config_version")
    private Integer testedConfigVersion;

    @Column(name = "last_webhook_at")
    private Instant lastWebhookAt;

    @Column(name = "last_webhook_event_type", length = 50)
    private String lastWebhookEventType;

    @Column(name = "last_webhook_failure_at")
    private Instant lastWebhookFailureAt;

    @Column(name = "last_webhook_failure_reason", length = 300)
    private String lastWebhookFailureReason;

    @Column(name = "webhook_failure_count", nullable = false)
    private int webhookFailureCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by_customer_id")
    private Long updatedByCustomerId;
}
