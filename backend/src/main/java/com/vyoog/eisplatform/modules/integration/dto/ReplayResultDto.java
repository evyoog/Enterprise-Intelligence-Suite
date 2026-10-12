package com.vyoog.eisplatform.modules.integration.dto;

import java.util.List;

/** The outcome (or, for a preview, the size) of a replay. */
public record ReplayResultDto(
    boolean preview,
    String receiver,
    long matched,
    int replayed,
    int failed,
    int limit,
    List<Failure> failures
) {
    public record Failure(String eventId, String eventType, String error) {
    }
}
