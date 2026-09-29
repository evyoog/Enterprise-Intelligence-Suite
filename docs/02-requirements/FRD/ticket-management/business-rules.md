# Business rules — Ticket Management

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-SUP-001 | A new ticket always starts as OPEN with MEDIUM priority, regardless of what the request contains — only subject and description are customer-supplied. | backend | REQ-SUP-001.1 |
| BR-SUP-002 | Every action resolves the ticket and, for a customer's own read, confirms `requestedByCustomerId == caller`, or refuses with the same 404 a nonexistent id would give. | backend | REQ-SUP-001.2 |
| BR-SUP-003 | Categorize/Prioritize/Assign (`updateTicket`) is refused (400) if the ticket is RESOLVED or CLOSED. Assigning (`assignedToCustomerId` non-null) an OPEN ticket also sets it to IN_PROGRESS; assigning an already IN_PROGRESS/ESCALATED ticket leaves its status alone. | backend | REQ-SUP-001.3, .7 |
| BR-SUP-004 | Escalate is refused (400) if the ticket is RESOLVED or CLOSED; otherwise it sets status to ESCALATED and priority to URGENT unconditionally. | backend | REQ-SUP-001.4, .7 |
| BR-SUP-005 | Resolve is refused (400) if the ticket is already RESOLVED or CLOSED; otherwise it sets status to RESOLVED, records the optional resolution note, and stamps `resolvedAt`. | backend | REQ-SUP-001.5, .7 |
| BR-SUP-006 | Close is refused (400) unless the ticket's current status is exactly RESOLVED; it then sets status to CLOSED and stamps `closedAt`. | backend | REQ-SUP-001.6 |
| BR-SUP-007 | Escalating or resolving a ticket notifies its requester (`NotificationCategory.SYSTEM`). Every mutation is recorded in the audit log (`TICKET_CREATED`/`_UPDATED`/`_ESCALATED`/`_RESOLVED`/`_CLOSED`). | backend | REQ-SUP-001.4, .5 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
