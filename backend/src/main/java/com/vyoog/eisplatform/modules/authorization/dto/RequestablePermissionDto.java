package com.vyoog.eisplatform.modules.authorization.dto;

import com.vyoog.eisplatform.modules.authorization.model.RoleScope;

/** One entry of {@code GET /me/privileged-access/requestable-permissions} (decision C24, REQ-IAM-004). */
public record RequestablePermissionDto(String permissionName, RoleScope scope, String description) {
}
