package com.vyoog.eisplatform.modules.toolsync.model;

/** ACTIVE connectors receive messages; PAUSED ones keep their pending deliveries and receive nothing new until resumed. */
public enum ConnectorStatus {
    ACTIVE, PAUSED
}
