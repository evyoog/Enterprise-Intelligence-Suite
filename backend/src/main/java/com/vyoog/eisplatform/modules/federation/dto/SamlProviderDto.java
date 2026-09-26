package com.vyoog.eisplatform.modules.federation.dto;

import java.time.Instant;

/**
 * {@code certificatePem} is included directly (not hidden/redacted) — an
 * IdP's signing certificate is inherently public data, already published in
 * that IdP's own metadata endpoint; there's nothing to protect by hiding it
 * from the very organization admin who configured it. Never a private
 * key — this app never has one of its own (see the entity's own javadoc).
 */
public record SamlProviderDto(
    Long id,
    String name,
    String entityId,
    String ssoUrl,
    String certificatePem,
    String certificateFingerprint,
    Instant certificateExpiresAt,
    boolean certificateExpired,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt,
    /** REQ-IAM-007: configured names; blank = default. */
    ClaimMappingDto claimMapping
) {
}
