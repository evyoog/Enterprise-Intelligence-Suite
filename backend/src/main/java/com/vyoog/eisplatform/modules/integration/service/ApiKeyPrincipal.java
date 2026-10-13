package com.vyoog.eisplatform.modules.integration.service;

import java.util.List;

/** REQ-INT-001.1: who a valid API key acts as (BR-3). For an organization key the acting person is an active
 * administrator of the organization (C92); {@code scope} is READ or READ_WRITE. */
public record ApiKeyPrincipal(
    Long keyId,
    String keyPrefix,
    Long ownerCustomerId,
    String ownerKeycloakSub,
    String ownerEmail,
    List<String> authorities,
    Long organizationId,
    String scope
) {
    public boolean readOnly() {
        return "READ".equals(scope);
    }
}
