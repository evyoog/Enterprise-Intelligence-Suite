package com.vyoog.eisplatform.modules.registration.dto;

/** Admin-only listing used to pick a parent organization — see
 * Organization's own javadoc on why that linkage is never public. */
public record OrganizationSummaryDto(Long id, String name, String code) {
}
