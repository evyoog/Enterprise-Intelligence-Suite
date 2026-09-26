package com.vyoog.eisplatform.modules.federation.dto;

import java.time.Instant;

/** REQ-IAM-006: never carries the client secret, only whether one is set (C27).
 * {@code redirectUri} is what the organization registers at its provider. */
public record OidcProviderDto(
    Long id,
    String name,
    String issuerUrl,
    String clientId,
    boolean clientSecretSet,
    String scopes,
    boolean enabled,
    String redirectUri,
    Instant createdAt,
    Instant updatedAt,
    /** REQ-IAM-007: configured names; blank = default. */
    ClaimMappingDto claimMapping
) {
}
