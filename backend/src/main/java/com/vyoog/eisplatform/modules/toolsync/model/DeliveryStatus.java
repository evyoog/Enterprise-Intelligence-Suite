package com.vyoog.eisplatform.modules.toolsync.model;

/** BR-SYN-006: DELIVERED only when the tool answered {@code applied} or {@code duplicate}; FAILED after a {@code rejected} answer or the retry limit. */
public enum DeliveryStatus {
    PENDING, DELIVERED, FAILED
}
