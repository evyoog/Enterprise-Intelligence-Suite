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

12.03 SLA Management and 12.04 Incident & Problem Management are **not** covered by this requirement — see [C40](../../../01-business/roadmap/open-decisions.md#c40).

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

## Out of scope
- Any AI-driven categorization/prioritization/response (12.02 AI Support / 04b, later sprint — needs an agent/LLM framework this platform doesn't have yet, same reasoning as 04a and 11.01.02).
- SLA policies, targets, or breach monitoring (12.03) — needs its own response/resolution-time data model, not invented here.
- Incident and Problem records (12.04) — overlaps with the existing `service_incident` table (C20) without a decided relationship between the two.
- Reopening a resolved or closed ticket.
- Multi-level escalation routing or an on-call chain — escalation here only raises priority and status.

## Dependencies
- Existing `Customer`, `CustomerRepository`, `NotificationService`, `AuditService`.
- New table `support_ticket` ([V010](../../../../database/migrations/V010__recommendations_ticket_management.sql)).
- New permission `MANAGE_SUPPORT_TICKETS` (platform ADMIN).
