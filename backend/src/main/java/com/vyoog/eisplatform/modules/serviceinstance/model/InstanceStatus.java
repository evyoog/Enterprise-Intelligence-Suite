package com.vyoog.eisplatform.modules.serviceinstance.model;

/** C101 SR-5: the status of a service instance. PROVISIONING until the product's first report, then ACTIVE; SUSPENDED and DEPROVISIONED follow the subscription. */
public enum InstanceStatus {
    PROVISIONING, ACTIVE, SUSPENDED, FAILED, DEPROVISIONED
}
