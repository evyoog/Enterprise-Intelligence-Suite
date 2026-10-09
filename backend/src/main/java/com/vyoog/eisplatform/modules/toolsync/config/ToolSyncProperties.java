package com.vyoog.eisplatform.modules.toolsync.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Settings of the platform's side of the platform ↔ tool synchronization (REQ-INT-003), {@code app.sync.*}. Secrets come from the
 * environment only: {@code SYNC_CLIENT_SECRET} is the secret of the platform's Keycloak service client ({@code eis-sync}); it has no
 * default and is never logged. See {@code deployment/keycloak-sync-clients.md}.
 */
@Component
@Getter
public class ToolSyncProperties {

    @Value("${app.sync.client-id:eis-sync}")
    private String clientId;

    @Value("${app.sync.client-secret:}")
    private String clientSecret;

    @Value("${app.sync.token-url:}")
    private String tokenUrl;

    @Value("${app.sync.timeout-seconds:10}")
    private int timeoutSeconds;

    /** Contract v1 defaults: up to 8 attempts, exponential backoff from 5 s to 15 min. */
    @Value("${app.sync.retry.max-attempts:8}")
    private int maxAttempts;

    @Value("${app.sync.retry.initial-delay-seconds:5}")
    private long initialDelaySeconds;

    @Value("${app.sync.retry.max-delay-seconds:900}")
    private long maxDelaySeconds;

    @Value("${app.sync.batch-size:100}")
    private int batchSize;

    /** The datasource reference sent in {@code provision_tenant}: a name the TOOL has in its own configuration, never a URL. */
    @Value("${app.sync.default-datasource-ref:default}")
    private String defaultDatasourceRef;

    /** How long idempotency records of calls from tools are kept (contract v1 section 5: at least 7 days). */
    @Value("${app.sync.idempotency-retention-days:7}")
    private int idempotencyRetentionDays;

    public boolean gatewayConfigured() {
        return !clientSecret.isBlank() && !tokenUrl.isBlank() && !clientId.isBlank();
    }
}
