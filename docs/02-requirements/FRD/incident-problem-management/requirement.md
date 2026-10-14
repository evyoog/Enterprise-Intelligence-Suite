# REQ-SUP-003 — Incident and problem management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-14 ([C101](../../../01-business/roadmap/open-decisions.md#c101), IC-1 A, IC-2 A). The engineering defaults listed under [Engineering defaults](#engineering-defaults) are applied "to confirm"; none blocks use of the feature.
**Decision:** [C101](../../../01-business/roadmap/open-decisions.md#c101) (IC-1, IC-2). Settles the open relationship to `service_incident` that [C40](../../../01-business/roadmap/open-decisions.md#c40) named, and the incident switch of the [C20](../../../01-business/roadmap/open-decisions.md#c20) retirement task.
**Built:** 2026-10-14 (migration [V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql), apply by hand: Flyway is disabled). Test cases: [TESTPLAN-SUP-003](../../../../test-cases/functional/incident-problem-management/TESTPLAN-SUP-003.md).

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-SUP-003 |
| Application | [12 Support & Service Management](../../../01-business/roadmap/applications/12-support-service-management.md) |
| Application code | `APP-SUP` |
| Priority | P0 ([C5](../../../01-business/roadmap/open-decisions.md#c5), C20 addendum) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 12.04.01.01 | Log incident | Yes: a new incident is `LOGGED` (.1, .3) |
| 12.04.01.02 | Investigate incident | Yes: phase `INVESTIGATING`, internal notes, assignee, severity, linked tickets (.3 to .6) |
| 12.04.01.03 | Resolve incident | Yes: phase `RESOLVED` sets the end time and needs a resolution summary (.3) |
| 12.04.01.04 | Close incident | Yes: phase `CLOSED`, only after `RESOLVED` (.3) |
| 12.04.02.01 | Create problem | Yes (.7, .8) |
| 12.04.02.02 | Perform root cause analysis | Yes: the problem's root cause and the status `ROOT_CAUSE_IDENTIFIED` (.9) |
| 12.04.02.03 | Track corrective action | Yes: corrective actions with owner, due date and done (.10) |

## Summary
A **support incident is the status-page incident**: one record (`service_incident`) with **public fields** that customers who bought the product see on the status page (title, message, product, start and end time) and **internal fields** that only platform administrators see (severity, phase, internal notes, assignee, resolution summary, linked tickets, the problem it belongs to) ([C101](../../../01-business/roadmap/open-decisions.md#c101) IC-1 A). An incident moves through **Logged, Investigating, Resolved, Closed**. A **problem** is a **separate record** that links several incidents to one **root cause** and tracks **corrective actions** (IC-2 A); it moves through **Open, Root cause identified, Resolved, Closed**. Platform administrators with `MANAGE_SERVICE_STATUS` manage both, on the **Incidents** and **Problems** tabs of Platform → Service status. **Customers never see the internal fields.**

## Actors
- **Platform administrator** (`MANAGE_SERVICE_STATUS`): logs, investigates, resolves and closes incidents; links tickets; creates problems, records the root cause and tracks corrective actions.
- **Customer** (a purchaser of the product): sees the public fields of the product's incidents on the status page.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUP-003.1 | A **support incident and a status-page incident are the same record** ([C101](../../../01-business/roadmap/open-decisions.md#c101) IC-1 A). An incident posted on the status page is a support incident and the other way round. The admin Manage tab of the status page no longer posts incidents; they are managed on the **Incidents** tab. | Must |
| REQ-SUP-003.2 | **Public fields:** title, message, product, start time and end time. They appear on the status page for customers who purchased the product ([REQ-PRT-001](../service-status-page/requirement.md)). **Internal fields:** severity, phase, internal notes, assignee, resolution summary, linked tickets and problem. They are **never returned to customers** and never shown on the status page. (The root cause is kept on the problem, not on the incident.) | Must |
| REQ-SUP-003.3 | **Phases.** `LOGGED` (new), `INVESTIGATING`, `RESOLVED`, `CLOSED`. A logged or investigating incident has **no end time**. **Resolved** sets the end time (the time given, else now) and needs a **resolution summary**. **Closed** is allowed **only after Resolved** and is final: a closed incident cannot be changed. A resolved incident can be **reopened** by removing its end time (it becomes Investigating). | Must |
| REQ-SUP-003.4 | **Severity** is `MINOR` (default), `MAJOR` or `CRITICAL`; internal. | Must |
| REQ-SUP-003.5 | An incident has an **assignee** (an existing customer account that handles it) and **internal notes**; internal. | Must |
| REQ-SUP-003.6 | **Support tickets can be linked** to an incident (the customers who reported the same trouble) and unlinked, except on a closed incident. Linking the same ticket twice changes nothing. | Must |
| REQ-SUP-003.7 | A **problem** is a **separate record** with a title, a description, a status, a root cause and corrective actions. | Must |
| REQ-SUP-003.8 | A problem **links several incidents** (when it is created or later) and they can be removed again; **an incident belongs to at most one problem**; a closed problem takes no further incidents. | Must |
| REQ-SUP-003.9 | **Problem statuses:** `OPEN`, `ROOT_CAUSE_IDENTIFIED` (needs a root cause), `RESOLVED` (needs a root cause) and `CLOSED` (only after Resolved, and only when **every corrective action is done**). A closed problem cannot be changed. | Must |
| REQ-SUP-003.10 | A problem has **corrective actions**: a description, an optional owner (free text), an optional due date and a done flag (with the time it was done). Actions can be added, changed and deleted until the problem is closed. | Must |
| REQ-SUP-003.11 | Incidents and problems are managed with the **platform permission `MANAGE_SERVICE_STATUS`**, the same as the status page. | Must |
| REQ-SUP-003.12 | Every change is **audited** (`INCIDENT_POSTED`, `INCIDENT_UPDATED`, `INCIDENT_TICKET_LINKED`, `INCIDENT_TICKET_UNLINKED`, `PROBLEM_CREATED`, `PROBLEM_UPDATED`, `PROBLEM_INCIDENT_LINKED`, `PROBLEM_INCIDENT_UNLINKED`, `PROBLEM_ACTION_ADDED`, `PROBLEM_ACTION_UPDATED`, `PROBLEM_ACTION_DELETED`). | Must |
| REQ-SUP-003.13 | **Incidents that existed before this change** keep their public fields; a resolved one (it has an end time) becomes phase `RESOLVED`, the others `INVESTIGATING`; severity `MINOR`. | Must |

## Engineering defaults
Applied to make C101 work; **to confirm** with the product owner.

| Topic | Default | Where |
|---|---|---|
| Phase when none is sent | New incident: `LOGGED`. Update: worked out from the end time (an end time = `RESOLVED`; none on a resolved incident = reopened as `INVESTIGATING`; otherwise unchanged) | `IncidentManagementService#apply` |
| Severity values | Minor, Major, Critical | `IncidentSeverity` |
| Customers on the status page | A customer who purchased the product sees the incident from the moment it is logged, with its public fields; "open" means "no end time", so Logged and Investigating are open, Resolved and Closed are resolved | `ServiceStatusService#customerView` |
| Assignee | Any existing customer id; an unknown id is refused | `IncidentManagementService#apply` |
| Problem links | An incident is moved to another problem only by unlinking it first | `ProblemService#link` |
| Corrective action owner | Free text up to 100 characters, not a user reference | `ProblemAction` |
| Lists | Incidents: latest 200 by start time; problems: newest first | repositories |

## Out of scope
**Not specified:** automatic creation of incidents from health reports or from SLA breaches, incident SLAs, major-incident communication to customers beyond the public fields, post-incident reviews, a problem record visible to customers, notifications about incidents or problems, categories and impact/urgency matrices, linking incidents to service instances (REQ-SRM-001), email-in of incidents. Customer-visible incident phases or severity.

## Dependencies
Status page ([REQ-PRT-001](../service-status-page/requirement.md), the `service_incident` table), ticket management ([REQ-SUP-001](../ticket-management/requirement.md)), the platform permission `MANAGE_SERVICE_STATUS`, audit, tables from [V041](../../../../database/migrations/V041__service_instances_sla_incidents_problems.sql).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [incident-problem-management.md](../../../05-ui/screen-requirements/incident-problem-management.md) |
| API | [api-requirements.md](api-requirements.md) → [incident-problem-management.md](../../../06-api/api-requirements/incident-problem-management.md) |
| Data model | [service-instances-sla-incidents.md](../../../07-database/data-model/service-instances-sla-incidents.md#incidents-and-problems-req-sup-003) (V041) |
| Test cases | [TESTPLAN-SUP-003](../../../../test-cases/functional/incident-problem-management/TESTPLAN-SUP-003.md) |

## Decisions ([C101](../../../01-business/roadmap/open-decisions.md#c101), product owner, 2026-10-14)
| # | Question | Answer |
|---|---|---|
| IC-1 | Relationship of a support incident to the status-page incident | **A:** the same record; title, public message, product, start and end are public; severity, internal notes, assignee, root cause, resolution summary, linked tickets and problem are internal |
| IC-2 | Relationship of a problem to incidents | **A:** a separate record that links several incidents, with a root cause and corrective actions |

## Open points
1. **The root cause is on the problem, not on the incident.** IC-1 lists "root cause" among the internal incident fields; the build keeps one root cause per problem, and an incident's explanation is its resolution summary. Confirm.
2. **Customers see an incident as soon as it is logged.** Whether a Logged incident should wait for an administrator to publish it is Not specified; today the public fields are shown to purchasers from the start, as before C101.
3. **Not specified:** automatic incident creation from health reports, notifications about incidents, customer-visible severity or phase, post-incident reviews.
