package com.vyoog.eisplatform.modules.federation.dto;

import java.util.List;

/**
 * Phase 4 (foundation) scope: validates the STORED configuration and checks
 * the SSO URL is reachable — this is NOT a real SAML login round-trip
 * (that needs the actual SP-initiated redirect + assertion consumption
 * Phase 5 builds). {@code checks} lists each individual thing verified, so
 * "Test" never silently claims more than it actually checked.
 */
public record SamlProviderTestResultDto(boolean success, List<String> checks, List<String> errors) {
}
