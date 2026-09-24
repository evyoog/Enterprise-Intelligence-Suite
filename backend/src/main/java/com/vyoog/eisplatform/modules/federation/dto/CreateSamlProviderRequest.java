package com.vyoog.eisplatform.modules.federation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Two ways to provide the same information — never both required at once:
 * <ul>
 *   <li>{@code metadataXml} set: entity id / SSO URL / certificate are all
 *   parsed out of it automatically (see SamlProviderService); the manual
 *   fields below are ignored if also present.</li>
 *   <li>{@code metadataXml} blank/absent: {@code entityId}/{@code ssoUrl}/
 *   {@code certificatePem} must all be supplied directly.</li>
 * </ul>
 */
public record CreateSamlProviderRequest(
    @NotBlank String name,
    String metadataXml,
    String entityId,
    String ssoUrl,
    String certificatePem
) {
}
