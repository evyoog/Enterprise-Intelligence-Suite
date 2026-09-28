package com.vyoog.eisplatform.modules.support.dto;

import com.vyoog.eisplatform.modules.support.model.TicketPriority;

/** 12.01.01.02/.03/.04 Categorize/Prioritize/Assign ticket (sprint
 * 2027.1.2), folded into one call — each field is optional; a null field
 * leaves that part of the ticket unchanged. */
public record UpdateTicketRequest(String category, TicketPriority priority, Long assignedToCustomerId) {
}
