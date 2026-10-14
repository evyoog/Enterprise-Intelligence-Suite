package com.vyoog.eisplatform.modules.billing.service.gateway;

/** The outcome of a connection test, or what is known without one (C98, REQ-BIL-003.6). */
public enum ConnectionStatus {
    NOT_CONFIGURED,
    CONFIGURATION_INCOMPLETE,
    NOT_TESTED,
    CONNECTION_SUCCESSFUL,
    AUTHENTICATION_FAILED,
    PROVIDER_UNAVAILABLE,
    UNSUPPORTED_CONFIGURATION,
    TEST_FAILED
}
