# REQ-SUP-002 — SLA management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-14 ([C101](../../../01-business/roadmap/open-decisions.md#c101), SL-1 B, SL-2 B). The engineering defaults listed under [Engineering defaults](#engineering-defaults) are applied "to confirm"; none blocks use of the feature.
**Decision:** [C101](../../../01-business/roadmap/open-decisions.md#c101) (SL-1, SL-2). Supersedes the "SLA is not built" line of [C40](../../../01-business/roadmap/open-decisions.md#c40) for 12.03.
**Built:** 2026-10-14 (migration [V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql), apply by hand: Flyway is disabled). Test cases: [TESTPLAN-SUP-002](../../../../test-cases/functional/sla-management/TESTPLAN-SUP-002.md).

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-SUP-002 |
| Application | [12 Support & Service Management](../../../01-business/roadmap/applications/12-support-service-management.md) |
| Application code | `APP-SUP` |
| Priority | Commit in the sprint page; the capability's priority is Not specified in the application page |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 12.03.01.01 | Define SLA | Yes: the targets per priority and the business hours are settings a platform administrator edits (.1, .3, .12) |
| 12.03.01.02 | Assign SLA | Yes: every new ticket gets the deadlines of its priority automatically; there is no per-plan or per-customer SLA (.2, .5) |
| 12.03.01.03 | Calculate SLA | Yes: deadlines are calculated in business hours from the creation time (.4, .5, .8) |
| 12.03.02.01 | Monitor SLA | Yes: a check every minute (.9, .10) |
| 12.03.02.02 | Warn before breach | Yes: at 80% of a target (.9) |
| 12.03.02.03 | Escalate breach | Yes: the ticket is escalated and flagged (.10) |

## Summary
Every support ticket ([REQ-SUP-001](../ticket-management/requirement.md)) gets two deadlines from its priority: a **first-response** deadline and a **resolution** deadline. The targets are the same for every customer and plan ([C101](../../../01-business/roadmap/open-decisions.md#c101) SL-2 B). The clock starts when the ticket is created and never pauses. When 80% of a target has passed the **assignee and the alert list are warned**; when the deadline passes the ticket is **escalated (status ESCALATED, priority unchanged) and flagged as breached**, and the same people are told. Platform administrators (`MANAGE_SUPPORT_TICKETS`) see an SLA state on every ticket and edit the targets, the business hours, the warning point and the alert list. **Customers see only the deadlines**, never the SLA states.

## Targets (SL-1 B)
| Priority | First response | Resolution | Clock | Stored as (minutes) |
|---|---|---|---|---|
| Urgent | 1 hour | 8 hours | **24x7** (around the clock) | 60 / 480 |
| High | 4 hours | 1 day | Business hours | 240 / 480 |
| Medium | 8 hours | 3 days | Business hours | 480 / 1440 |
| Low | 1 day | 5 days | Business hours | 480 / 2400 |

**Business hours** (settings; defaults): **Monday to Friday, 09:00 to 18:00, time zone Asia/Kolkata**. **"1 day" means 8 business hours = 480 minutes**, and a target counts only minutes inside the business window, so a High ticket created on Monday at 09:00 is due on Monday at 17:00 and a Medium ticket created then has its resolution deadline on Wednesday at 15:00. Public holidays are not modelled. The clock never pauses, including while the ticket waits for the customer.

## Actors
- **Platform administrator** (`MANAGE_SUPPORT_TICKETS`): sees the SLA state of every ticket, edits the targets and settings, and works the tickets (REQ-SUP-001).
- **Assignee** (the support person a ticket is assigned to): is warned and told of a breach.
- **People on the alert list** (email addresses): are warned and told of a breach.
- **Customer** (the ticket's requester): sees the deadlines on their own tickets.
- **System** (the SLA check, every minute): sets deadlines, warns and escalates.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUP-002.1 | There is one **SLA target per ticket priority** (`LOW`, `MEDIUM`, `HIGH`, `URGENT`): a first-response target and a resolution target in minutes, and whether the priority runs around the clock. The seeded values are the [SL-1 B targets](#targets-sl-1-b). | Must |
| REQ-SUP-002.2 | The targets are **the same for every customer and plan** ([C101](../../../01-business/roadmap/open-decisions.md#c101) SL-2 B). | Must |
| REQ-SUP-002.3 | **Business hours** are a setting: working days, start and end time and time zone, seeded as Monday to Friday 09:00 to 18:00, Asia/Kolkata. **Urgent runs 24x7.** "1 day" is 8 business hours (480 minutes). **Public holidays are not modelled.** | Must |
| REQ-SUP-002.4 | The **clock starts when the ticket is created** and **never pauses**. A ticket created outside business hours starts counting at the next opening. | Must |
| REQ-SUP-002.5 | A new ticket gets a **first-response deadline** and a **resolution deadline** from its creation time and priority (a new ticket is MEDIUM). | Must |
| REQ-SUP-002.6 | The **first response** is the **first time a person takes the ticket out of Open**: assigning it, escalating it or resolving it. Categorizing or changing the priority is not a response. The automatic escalation of a breach is not a response either. It stops the first-response clock and is stored on the ticket. | Must |
| REQ-SUP-002.7 | The **resolution** is the time the ticket is resolved. | Must |
| REQ-SUP-002.8 | **Changing a ticket's priority recalculates both deadlines from the creation time** (not from the time of the change). Escalating a ticket raises it to URGENT and recalculates in the same way. | Must |
| REQ-SUP-002.9 | A **warning** goes out when **80%** of a target has elapsed (the warning point is a setting, 1 to 99%), once per target, to the ticket's assignee and the people on the alert list. | Must |
| REQ-SUP-002.10 | A **breach** (the deadline passes before the first response, or before resolution) **escalates the ticket (status ESCALATED, priority unchanged)** and **flags** it, once per target, and tells the same people. | Must |
| REQ-SUP-002.11 | **Alert recipients:** the **assignee** and the **alert email list** (up to 20 addresses). People who have an account (the assignee, or a listed address that matches an account) get an **in-app notice** and the usual notification email unless they muted the category; an address with no account gets an email. Nobody is told twice about the same alert. | Must |
| REQ-SUP-002.12 | A platform administrator with `MANAGE_SUPPORT_TICKETS` **edits** the targets (per priority), the business hours, the warning point and the alert list on the **SLA settings** screen. **New targets apply to tickets created or re-prioritised afterwards**; deadlines already set are not moved. Changes are audited. | Must |
| REQ-SUP-002.13 | **What people see.** Platform administrators see, on each ticket row, a first-response chip and a resolution chip with the state (`ON_TRACK`, `WARNING`, `BREACHED`, `MET`, `MET_LATE`) and the due time, can **filter by SLA state**, and see a breach flag. **Customers see only the deadlines** ("we aim to respond by … and resolve by …"), never the states. | Must |
| REQ-SUP-002.14 | **Tickets created before SLA tracking have no deadlines**: no chips, no states, no warnings, no breaches, and a priority change does not give them deadlines. | Must |

## Engineering defaults
Applied to make C101 work; **to confirm** with the product owner. None changes an answer of C101.

| Topic | Default | Where |
|---|---|---|
| Settings editable | The targets, business hours, time zone, warning point and alert list are editable settings, seeded with the C101 values | `SlaService#updatePolicies`, `#updateSettings`, tables `sla_policy`, `sla_settings` |
| Day length | "1 day" = 480 minutes of business time, although the business window is 9 hours | seed in V041 |
| Warning point | 80% of the target's minutes, counted in business time (24x7 for Urgent); at least 1 minute | `SlaCalculator#addPercent` |
| First response | First assign, escalate or resolve by a person; an automatic breach escalation does not count | `SupportTicketService#markFirstResponse` |
| Breach | Status ESCALATED, priority **unchanged** (a manual escalate also raises the priority to URGENT; an automatic one does not) | `SlaService#breach` |
| Late response | A ticket answered after its deadline shows `MET_LATE` | `SlaService#state` |
| Check frequency | Every minute (`app.sla.interval-ms`); off with `app.sla.job-enabled=false` | `SlaCheckJob` |
| Targets accepted | 1 minute to one year; first response not longer than resolution | `SlaService#updatePolicies` |
| Alert list | Up to 20 valid addresses, stored lowercase, duplicates removed | `SlaService#updateSettings` |

## Out of scope
**Not specified:** SLA by plan or per customer (SL-2 B says the same for everyone), public holidays, pausing the clock while waiting for the customer, an on-call chain or multi-level escalation, customer-facing breach notices, SLA reports and trends, SLA on incidents.

## Dependencies
Ticket management ([REQ-SUP-001](../ticket-management/requirement.md)), notifications and email, audit, the platform permission `MANAGE_SUPPORT_TICKETS`, tables from [V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [sla-management.md](../../../05-ui/screen-requirements/sla-management.md) |
| API | [api-requirements.md](api-requirements.md) → [sla-management.md](../../../06-api/api-requirements/sla-management.md) |
| Data model | [service-instances-sla-incidents.md](../../../07-database/data-model/service-instances-sla-incidents.md#sla-management-req-sup-002) (V041) |
| Test cases | [TESTPLAN-SUP-002](../../../../test-cases/functional/sla-management/TESTPLAN-SUP-002.md) |

## Decisions ([C101](../../../01-business/roadmap/open-decisions.md#c101), product owner, 2026-10-14)
| # | Question | Answer |
|---|---|---|
| SL-1 | Response and resolution targets | **B:** Urgent 1 h / 8 h (24x7); High 4 h / 1 day, Medium 8 h / 3 days, Low 1 day / 5 days (business hours) |
| SL-2 | Per plan or the same for all | **B:** the same for every customer and plan |

## Open points
1. **The targets are editable settings** (the product owner asked for the values, not for a screen); confirm the screen is wanted.
2. **A breach escalates without raising the priority.** A person who escalates by hand still raises it to URGENT (REQ-SUP-001.4).
3. **Not specified:** SLA by plan, public holidays, pausing the clock while waiting for the customer.
4. **Tickets created before this build have no deadlines** ([REQ-SUP-002.14](#functional-requirements)); a bulk back-fill is not built.
