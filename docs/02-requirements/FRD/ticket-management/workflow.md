# Workflow — Ticket Management

## States
```mermaid
stateDiagram-v2
    [*] --> OPEN: createTicket (BR-SUP-001)
    OPEN --> IN_PROGRESS: updateTicket assigns (BR-SUP-003)
    IN_PROGRESS --> ESCALATED: escalateTicket (BR-SUP-004)
    OPEN --> ESCALATED: escalateTicket (BR-SUP-004)
    OPEN --> RESOLVED: resolveTicket (BR-SUP-005)
    IN_PROGRESS --> RESOLVED: resolveTicket (BR-SUP-005)
    ESCALATED --> RESOLVED: resolveTicket (BR-SUP-005)
    RESOLVED --> CLOSED: closeTicket (BR-SUP-006)
    CLOSED --> [*]
```
**SLA breach (C101):** the scheduled SLA check also moves an OPEN or IN_PROGRESS ticket to ESCALATED when a deadline passes, without changing its priority ([REQ-SUP-002](../sla-management/workflow.md)). Categorize/Prioritize/Assign do not change status by themselves except the OPEN→IN_PROGRESS move on assignment (BR-SUP-003); every other transition above is the whole effect of its own action. RESOLVED and CLOSED are otherwise terminal — no action in this feature reopens a ticket.

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| (new) | OPEN | Customer | BR-SUP-001 | `TICKET_CREATED` audit |
| OPEN | IN_PROGRESS | Admin | BR-SUP-003 (assign) | `TICKET_UPDATED` audit |
| OPEN / IN_PROGRESS | ESCALATED | Admin | BR-SUP-004 | `TICKET_ESCALATED` audit; notifies the requester |
| OPEN / IN_PROGRESS / ESCALATED | RESOLVED | Admin | BR-SUP-005 | `TICKET_RESOLVED` audit; notifies the requester |
| RESOLVED | CLOSED | Admin | BR-SUP-006 | `TICKET_CLOSED` audit |
| OPEN / IN_PROGRESS | ESCALATED | System (SLA check) | A deadline passed; BR-SLA-010 | `TICKET_SLA_BREACHED` audit; assignee and alert list told; priority unchanged |
