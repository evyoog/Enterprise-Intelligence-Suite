package com.vyoog.eisplatform.modules.integration.service;

/**
 * REQ-INT-002.3/.4: an in-application subscriber. Every Spring bean that
 * implements this receives the events whose type it {@link #handles}, at
 * least once, in order per aggregate. Each handler's processed events are
 * recorded, so a repeat delivery is skipped — but a handler should still be
 * safe to run twice (the receipt is written after it returns).
 */
public interface PlatformEventHandler {

    /** Stable identity used for receipts (max 100 characters). */
    String name();

    boolean handles(String eventType);

    void handle(PlatformEvent event);
}
