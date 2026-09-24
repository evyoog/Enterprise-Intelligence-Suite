package com.vyoog.eisplatform.modules.authorization.dto;

/** Name is immutable after creation — every enforcement point checks a
 * permission by name (see AuthorizationService#grants), so renaming one
 * would silently break every existing check that references the old name. */
public record UpdatePermissionRequest(String description) {
}
