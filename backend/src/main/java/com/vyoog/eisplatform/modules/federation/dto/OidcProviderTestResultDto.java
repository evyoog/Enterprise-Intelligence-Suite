package com.vyoog.eisplatform.modules.federation.dto;

import java.util.List;

/** Result of "Test": discovery reachable, issuer matches, required endpoints
 * present. It does not sign anyone in. */
public record OidcProviderTestResultDto(boolean success, List<String> checks, List<String> errors) {
}
