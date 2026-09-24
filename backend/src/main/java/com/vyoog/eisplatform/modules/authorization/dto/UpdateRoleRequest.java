package com.vyoog.eisplatform.modules.authorization.dto;

import java.util.List;

/** Name and scope are immutable after creation — both are load-bearing
 * identifiers (matched against a Keycloak client-role claim for PLATFORM,
 * or the fixed {@code OrgRole} enum for ORGANIZATION), so changing them
 * would silently orphan whatever already relies on the old value. Only the
 * description and permission set can be edited. */
public record UpdateRoleRequest(String description, List<Long> permissionIds) {
}
