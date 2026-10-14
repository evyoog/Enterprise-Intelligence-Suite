package com.vyoog.eisplatform.modules.serviceinstance.dto;

import java.time.Instant;
import java.util.List;

/** C101 / REQ-SRM-001: the service instance API. */
public final class ServiceInstanceDtos {

    private ServiceInstanceDtos() {
    }

    /** {@code stale}: no report in the last 5 minutes (so the health is not counted on the status page). {@code changedBy}: SYSTEM, PRODUCT or ADMIN. */
    public record Instance(Long id, Long subscriptionId, Long productId, String productName, String ownerType, Long ownerOrganizationId, String ownerName,
                           String status, String health, String healthMessage, Instant lastReportedAt, boolean stale,
                           String tenantReference, String accessUrl, String changedBy, String changeNote, Instant createdAt) {
    }

    /** What a platform administrator can set by hand (SR-5 B); both optional, a note is required. */
    public record ManualChange(String status, String health, String note) {
    }

    public record Summary(int total, int healthy, int degraded, int down, int unknown, int noRecentReport) {
    }

    public record ReporterKey(Long id, Long productId, String productName, String label, String keyPrefix, Instant createdAt, Instant lastUsedAt, Instant revokedAt) {
    }

    /** The key itself appears only in this answer to creating it. */
    public record CreatedReporterKey(ReporterKey key, String secret) {
    }

    public record CreateReporterKey(Long productId, String label) {
    }

    /** One report from a product: {@code instanceId} or {@code subscriptionId}; {@code status} is optional (ACTIVE, PROVISIONING or FAILED). */
    public record HealthReport(Long instanceId, Long subscriptionId, String health, String message, String status, String tenantReference, String accessUrl) {
    }

    public record HealthReports(List<HealthReport> reports) {
    }

    public record ReportResult(int accepted, int rejected, List<String> errors) {
    }
}
