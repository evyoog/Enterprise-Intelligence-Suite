package com.vyoog.eisplatform.modules.registration.dto;

/** ORG_ADMIN-only (MANAGE_ORGANIZATION) — see OrganizationSelfService#updateMfaPolicy. */
public record UpdateMfaPolicyRequest(boolean mfaRequired) {
}
