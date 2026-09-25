package com.vyoog.eisplatform.modules.registration.dto;

import java.util.List;

/**
 * REQ-TEN-001: the organization after a suspend / activate / close, plus how
 * many member logins Keycloak actually updated. {@code accountsNotUpdated}
 * lists the emails Keycloak refused or could not be reached for; the status
 * change itself still stands (BR-TEN-005) so the admin can retry.
 */
public record OrganizationLifecycleResultDto(
    OrganizationAdminDto organization,
    int accountsUpdated,
    List<String> accountsNotUpdated
) {
}
