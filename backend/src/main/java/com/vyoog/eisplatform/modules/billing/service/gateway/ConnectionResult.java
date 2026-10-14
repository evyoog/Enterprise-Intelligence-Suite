package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.time.Instant;

public record ConnectionResult(ConnectionStatus status, String message, Instant testedAt) {
    public boolean successful() {
        return status == ConnectionStatus.CONNECTION_SUCCESSFUL;
    }

    public static ConnectionResult of(ConnectionStatus status, String message) {
        return new ConnectionResult(status, message, Instant.now());
    }
}
