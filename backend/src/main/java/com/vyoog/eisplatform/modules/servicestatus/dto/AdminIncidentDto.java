package com.vyoog.eisplatform.modules.servicestatus.dto;

import java.time.Instant;
import java.util.List;

/** C101 / REQ-SUP-003: an incident as platform administrators see it: the public fields plus the internal ones. Never sent to customers. */
public record AdminIncidentDto(
    Long id,
    Long productId,
    String productName,
    String title,
    String message,
    Instant startedAt,
    Instant endedAt,
    boolean open,
    String severity,
    String phase,
    String internalNotes,
    Long assignedToCustomerId,
    String assignedToName,
    String resolutionSummary,
    Instant closedAt,
    Long problemId,
    String problemTitle,
    List<LinkedTicket> tickets
) {
    public record LinkedTicket(Long id, String subject, String status, String priority) {
    }
}
