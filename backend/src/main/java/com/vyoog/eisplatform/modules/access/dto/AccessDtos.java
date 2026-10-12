package com.vyoog.eisplatform.modules.access.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** REQ-TEN-005 (C90): request and response shapes of the Access management API. */
public final class AccessDtos {

    private AccessDtos() {
    }

    public record PermissionCatalogEntry(String code, String name, String description, String area, boolean callerCanGrant) {
    }

    /** One row of the Members tab. */
    public record MemberRow(Long memberId, Long customerId, String name, String email, String role, String status,
                            int productsOn, int productsEntitled, int customAccessCount,
                            Instant lastReviewedAt, String lastReviewedBy, boolean canChange, String lockedReason) {
    }

    public record MemberPage(List<MemberRow> items, long totalElements, int page, int size) {
    }

    public record SeatInfo(long inUse, int quantity) {
    }

    /** {@code enabled} = the member has the product now; {@code wanted} = what role default or override says; {@code source} = ROLE, OVERRIDE or PROJECT. */
    public record ProductItem(Long productId, String name, String plan, boolean enabled, boolean wanted, String source,
                              String productRole, SeatInfo seats, String lockedReason) {
    }

    /** {@code source} = LOCKED (organization admin), OVERRIDE or ROLE. */
    public record PermissionItem(String code, String name, String description, String area, boolean enabled, String source,
                                 String lockedReason) {
    }

    public record MemberDetail(Long memberId, String name, String email, String role, String status, Instant lastReviewedAt,
                               String lastReviewedBy, boolean canChange, String memberLockedReason,
                               List<ProductItem> products, List<PermissionItem> permissions) {
    }

    /** {@code true}/{@code false} stores an override, {@code reset} removes one ("permission:CODE", "product:7"). */
    public record SetOverridesRequest(Map<String, Boolean> permissions, Map<Long, Boolean> products, List<String> reset) {
    }

    public record RoleDefaultPermission(String code, String name, String description, String area, boolean enabled,
                                        boolean locked, boolean customized) {
    }

    public record RoleDefaultProduct(Long productId, String name, boolean enabled, boolean customized) {
    }

    public record RoleDefaults(String role, long memberCount, List<RoleDefaultPermission> permissions, List<RoleDefaultProduct> products) {
    }

    public record RoleDefaultsRequest(Map<String, Boolean> permissions, Map<Long, Boolean> products) {
    }

    public record RoleDefaultImpactItem(String item, String name, boolean from, boolean to, int affectedMembers) {
    }

    public record RoleDefaultsResult(boolean preview, int affectedMembers, int skippedNoSeat, List<RoleDefaultImpactItem> items,
                                     RoleDefaults defaults) {
    }

    /** {@code item} = "product:7" or "permission:MANAGE_ORDERS"; {@code action} = GRANT or REMOVE. */
    public record BulkRequest(String action, String item, List<Long> memberIds, boolean preview) {
    }

    public record BulkSkipped(Long memberId, String name, String reason) {
    }

    public record BulkResult(boolean preview, String item, String itemName, String action, int applied, List<BulkSkipped> skipped) {
    }
}
