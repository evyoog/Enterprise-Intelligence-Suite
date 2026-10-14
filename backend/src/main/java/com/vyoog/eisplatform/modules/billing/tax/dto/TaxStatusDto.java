package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** REQ-BIL-002.2: what Tax Configuration shows at the top: the active mode, its status and the numbers behind it. Never a secret. */
public record TaxStatusDto(
    String mode,
    String modeLabel,
    boolean externalEngineEnabled,
    String status,
    List<Message> messages,
    int configVersion,
    Instant modeChangedAt,
    String modeChangedBy,
    boolean allowSelfApproval,
    Internal internal,
    External external,
    long invoicesWaiting,
    LastCalculation lastCalculation
) {
    /** level: INFO, WARNING or ERROR. */
    public record Message(String level, String code, String text) {
    }

    public record Internal(long rulesInForce, long pendingApproval, long draftRules, List<String> countriesCovered) {
    }

    public record External(
        String providerCode,
        String providerName,
        List<ProviderOption> providers,
        boolean configured,
        Map<String, Boolean> credentials,
        String endpointHost,
        String environment,
        String companyCode,
        int timeoutSeconds,
        int maxAttempts,
        String validationStatus,
        Instant validatedAt,
        String validationSummary,
        List<String> countries,
        List<String> currencies,
        List<String> transactionTypes
    ) {
    }

    public record ProviderOption(String code, String name) {
    }

    public record LastCalculation(Instant at, String engine, String status, String reasonCode, String reasonMessage) {
    }
}
