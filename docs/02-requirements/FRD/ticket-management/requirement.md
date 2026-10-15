# REQ-SUP-001 — Ticket Management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C40](../../../01-business/roadmap/open-decisions.md#c40))

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-SUP-001 |
| Application | [12 Support & Service Management](../../../01-business/roadmap/applications/12-support-service-management.md) |
| Application code | `APP-SUP` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) — see Out of scope |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 12.01.01 | Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket | [12 Support & Service Management](../../../01-business/roadmap/applications/12-support-service-management.md#1201-support) |

12.03 SLA Management and 12.04 Incident & Problem Management are **not** covered by this requirement. They were not built at [C40](../../../01-business/roadmap/open-decisions.md#c40) and are now their own FRDs, built on [C101](../../../01-business/roadmap/open-decisions.md#c101) (2026-10-14): [REQ-SUP-002 SLA management](../sla-management/requirement.md) and [REQ-SUP-003 incident and problem management](../incident-problem-management/requirement.md).

## Summary
Any authenticated customer can raise a support ticket and track its status. A platform administrator (`MANAGE_SUPPORT_TICKETS`) categorizes, prioritizes, assigns, escalates, resolves and closes it. Every action here is performed by a human — the roadmap's "AI Agent" actor for these functions belongs to a later sprint's AI Support feature, which needs an agent/LLM framework this platform doesn't have yet.

## Actors
- Any authenticated customer — creates and tracks their own tickets
- Platform administrator (`MANAGE_SUPPORT_TICKETS`) — every other action

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUP-001.1 | Any authenticated customer can create a ticket (subject + description); it starts OPEN with MEDIUM priority. | Must |
| REQ-SUP-001.2 | A customer can list and read their own tickets; another customer's ticket is refused with a generic 404. | Must |
| REQ-SUP-001.3 | An admin can set a ticket's category, priority, and/or assignee in one call; assigning an OPEN ticket also moves it to IN_PROGRESS. | Must |
| REQ-SUP-001.4 | An admin can escalate a ticket, which sets its status to ESCALATED and its priority to URGENT. | Must |
| REQ-SUP-001.5 | An admin can resolve a ticket with an optional resolution note, recording when it was resolved. | Must |
| REQ-SUP-001.6 | An admin can close a ticket, but only once it is RESOLVED. | Must |
| REQ-SUP-001.7 | No action in this feature is available on a RESOLVED or CLOSED ticket except Close itself (from RESOLVED). | Must |
| REQ-SUP-001.8 | **SLA fields** (added 2026-10-14, [C101](../../../01-business/roadmap/open-decisions.md#c101)): a new ticket gets a first-response and a resolution deadline from its priority; the ticket records when a person first responded; changing the priority or escalating recalculates the deadlines; a missed deadline escalates the ticket (status ESCALATED, priority unchanged). Customers see the deadlines on their own tickets, administrators also the SLA states. Defined in [REQ-SUP-002](../sla-management/requirement.md). Tickets created before SLA tracking have no deadlines. | Must |
| REQ-SUP-001.9 | **Demo request** ([C102](../../../01-business/roadmap/open-decisions.md#c102), [REQ-MKT-005.10](../product-evaluation/requirement.md)): a signed-in customer's request for a demo of a product creates a ticket with the **category "Demo request"** already set (subject "Demo request: {product}"). It is an ordinary ticket in every other way: OPEN, MEDIUM, SLA deadlines, listed in My tickets and in Support tickets. The category is free text, like every ticket category. | Must |

## Out of scope
- Any AI-driven categorization/prioritization/response (12.02 AI Support / 04b, later sprint — needs an agent/LLM framework this platform doesn't have yet, same reasoning as 04a and 11.01.02).
- ~~SLA policies, targets, or breach monitoring (12.03)~~ — **now built** as [REQ-SUP-002](../sla-management/requirement.md) ([C101](../../../01-business/roadmap/open-decisions.md#c101), SL-1 B, SL-2 B). The ticket's SLA fields are described in REQ-SUP-001.8.
- ~~Incident and Problem records (12.04)~~ — **now built** as [REQ-SUP-003](../incident-problem-management/requirement.md) ([C101](../../../01-business/roadmap/open-decisions.md#c101), IC-1 A: a support incident is the status-page incident). An incident can be linked to tickets there; a ticket does not change.
- Reopening a resolved or closed ticket.
- Multi-level escalation routing or an on-call chain — escalation here only raises priority and status.

## Dependencies
- Existing `Customer`, `CustomerRepository`, `NotificationService`, `AuditService`.
- New table `support_ticket` ([V010](../../../../database/migrations/V010__recommendations_ticket_management.sql)).
- New permission `MANAGE_SUPPORT_TICKETS` (platform ADMIN).
- SLA columns on `support_ticket` ([V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql)) and the SLA check, see [REQ-SUP-002](../sla-management/requirement.md).
