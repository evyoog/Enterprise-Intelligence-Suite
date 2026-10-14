package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** C98 / REQ-BIL-003: the payment gateway management API. No record here ever carries a secret: a field says only whether it is set, where
 * its value comes from, and (for a public identifier) a masked form. */
public final class GatewayDtos {

    private GatewayDtos() {
    }

    public record Actions(boolean configure, boolean test, boolean activate, boolean deactivate, boolean setDefault) {
    }

    /** One row of the gateway list. {@code configurationStatus}: NOT_AVAILABLE (planned), NOT_CONFIGURED, CONFIGURATION_INCOMPLETE, CONFIGURED. */
    public record Gateway(
        String code, String name, String description, String logo,
        /** IMPLEMENTED or PLANNED. */
        String implementation,
        List<String> supportedMethods, List<String> enabledMethods, List<String> currencies, List<String> countries,
        String environment, String configurationStatus,
        /** A ConnectionStatus name. */
        String connectionStatus, String connectionMessage, Instant testedAt,
        boolean activated, boolean defaultGateway, Actions actions
    ) {
    }

    public record Summary(int registered, int implemented, int configured, int active, int needsConfiguration) {
    }

    public record Field(String key, String label, boolean secret, boolean required, boolean webhook, String help,
                        boolean configured,
                        /** STORED, ENVIRONMENT or NONE. */
                        String source,
                        /** A masked form of a non-secret identifier; always null for a secret. */
                        String maskedValue) {
    }

    public record Webhook(String url, boolean supported, boolean secretConfigured,
                          /** VERIFIED (a correctly signed event was accepted), NOT_SEEN, or NOT_APPLICABLE. */
                          String signatureStatus,
                          Instant lastEventAt, String lastEventType, int failureCount, Instant lastFailureAt, String lastFailureReason,
                          List<String> events,
                          /** False when the URL points at this machine, so the provider cannot reach it. */
                          boolean reachableFromInternet) {
    }

    /** Separate evidence that each capability has worked, taken from real records, not from the connection test. */
    public record Evidence(boolean authenticated, boolean webhookReceived, boolean paymentCaptured, boolean refundMade) {
    }

    public record WebhookEvent(String eventType, Instant receivedAt, Instant processedAt, Long paymentId) {
    }

    public record HistoryEntry(Instant at, String action, String actor, String detail) {
    }

    public record Detail(
        Gateway gateway, List<String> transactionTypes, List<String> environments,
        boolean webhookSupport, boolean refundSupport, boolean recurringSupport,
        List<Field> fields, List<String> setupSteps, Webhook webhook, Evidence evidence,
        int configVersion, Instant credentialsUpdatedAt, boolean encryptionAvailable, boolean credentialsUnreadable,
        List<WebhookEvent> recentEvents, List<HistoryEntry> history
    ) {
    }

    public record Connection(String status, String message, Instant testedAt, boolean successful) {
    }

    /** A blank value keeps the stored one. {@code environment} is TEST or LIVE. */
    public record SaveConfiguration(String environment, Map<String, String> fields) {
    }

    public record SetMethods(List<String> methods) {
    }

    public record SetDefault(String code) {
    }
}
