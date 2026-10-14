package com.vyoog.eisplatform.modules.billing.service.gateway;

/** Who made a gateway change. A platform administrator's Keycloak account may have no customer row, so the Keycloak subject is carried too. */
public record GatewayActor(String keycloakSub, Long customerId) {
}
