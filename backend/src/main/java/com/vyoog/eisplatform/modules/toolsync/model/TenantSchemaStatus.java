package com.vyoog.eisplatform.modules.toolsync.model;

/** Where an organization stands in a tool: PENDING (asked to provision or not yet), READY (provisioned, messages flow), FAILED (see last_error; retry from the monitor). */
public enum TenantSchemaStatus {
    PENDING, READY, FAILED
}
