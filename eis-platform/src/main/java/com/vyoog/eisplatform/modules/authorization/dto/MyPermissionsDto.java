package com.vyoog.eisplatform.modules.authorization.dto;

import java.util.List;

/**
 * Screen-level permission data for the frontend — real permission names, not
 * a single "isAdmin" boolean, so a UI can gate a specific action/button on
 * exactly the permission it needs instead of inferring it from a role name.
 * {@code organization} is empty for a caller with no organization membership
 * (including a platform admin with no Customer row at all — see
 * PermissionsController's own javadoc on why that's handled gracefully here).
 */
public record MyPermissionsDto(
    List<String> platform,
    List<String> organization
) {
}
