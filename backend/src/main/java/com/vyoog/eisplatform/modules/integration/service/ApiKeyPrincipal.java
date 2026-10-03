package com.vyoog.eisplatform.modules.integration.service;

import java.util.List;

/** REQ-INT-001.1: who a valid API key acts as (BR-3). */
public record ApiKeyPrincipal(
    Long keyId,
    String keyPrefix,
    Long ownerCustomerId,
    String ownerKeycloakSub,
    String ownerEmail,
    List<String> authorities
) {
}
