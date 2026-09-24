package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code keycloakSub} must be the Keycloak user's "sub" (their immutable
 * subject id) — never their email, per this phase's explicit constraint that
 * matching is always by sub. */
public record LinkKeycloakUserRequest(@NotBlank String keycloakSub) {
}
