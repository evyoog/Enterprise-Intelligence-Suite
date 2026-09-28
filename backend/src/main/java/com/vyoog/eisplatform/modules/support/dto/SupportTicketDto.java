package com.vyoog.eisplatform.modules.support.dto;

import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;

import java.time.Instant;

public record SupportTicketDto(
    Long id,
    Long requestedByCustomerId,
    String requestedByName,
    String subject,
    String description,
    String category,
    TicketPriority priority,
    TicketStatus status,
    Long assignedToCustomerId,
    String assignedToName,
    String resolutionNote,
    Instant resolvedAt,
    Instant closedAt,
    Instant createdAt
) {
}
