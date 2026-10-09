package com.vyoog.eisplatform.modules.toolsync.dto;

import java.time.Instant;
import java.util.List;

/** The Tool sync monitor (REQ-INT-003, admin tab "Tool sync"). */
public final class ToolSyncDtos {

    private ToolSyncDtos() {
    }

    public record TenantDto(Long organizationId, String organizationName, String status, String schemaVersion, Instant lastSyncedAt,
                            String lastError, long pending, long failed, Instant lastDeliveredAt) {
    }

    public record ConnectorDto(Long id, String productCode, String baseMcpUrl, String clientId, String status, List<TenantDto> tenants) {
    }

    public record DeliveryDto(Long id, String eventId, Long connectorId, String productCode, Long organizationId, String eventType,
                              String aggregateType, String aggregateId, String status, int attempts, Instant nextAttemptAt,
                              String lastError, Long sentVersion, Instant createdAt, Instant deliveredAt) {
    }

    public record DeliveryPageDto(List<DeliveryDto> items, long totalElements, int page, int size) {
    }

    /** What a reconcile found for one aggregate type. */
    public record ReconcileTypeDto(String aggregateType, long platformCount, Long toolCount, boolean inSync, int resent, int extraInTool) {
    }

    public record ReconcileDto(Long organizationId, String productCode, boolean reachable, String problem, boolean inSync, int resent,
                               List<ReconcileTypeDto> types) {
    }

    public record ActionResultDto(String message) {
    }
}
