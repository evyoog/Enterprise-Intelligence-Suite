package com.vyoog.eisplatform.modules.support.model;

/** 12.01.01 Ticket Management (sprint 2027.1.2). RESOLVED and CLOSED are
 * terminal — no action in this feature reopens a ticket (Not specified in
 * any source; not invented here). */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    ESCALATED,
    RESOLVED,
    CLOSED
}
