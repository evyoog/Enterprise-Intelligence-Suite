package com.vyoog.eisplatform.modules.integration.dto;

import java.time.LocalDate;

/** REQ-INT-002.9 (C91, EV-1 B): re-send the delivered events of a time range (UTC dates, inclusive) to one receiver. */
public record ReplayRequest(
    String receiver,
    LocalDate from,
    LocalDate to,
    String eventType,
    boolean preview
) {
}
