# TESTPLAN-SUP-003: Incident and problem management

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUP-003 |
| Requirement(s) covered | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Author | Not specified |
| Status | Draft (the FRD is Approved, 2026-10-14; built 2026-10-14; engineering defaults to confirm) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md): TC-SUP-021 to TC-SUP-036. Out of scope (Not specified): automatic incident creation, notifications about incidents, customer-visible phases.

## Test Strategy
| Level | Approach |
|---|---|
| Unit / integration | Backend: `IncidentAndProblemTest` (phases, links, problems, actions) and `ServiceStatusServiceTest` (what customers see) on the H2-backed Spring context; `ServiceStatusAuthorizationTest` (the permission rule). Frontend: `AdminIncidentsPage.test.tsx`, `AdminProblemsPage.test.tsx` and `ServiceStatusPage.test.tsx` with jest-axe. Manual: requests to the new endpoints without the permission, audit entries, the migration. |
| Security (SAST/DAST) | Security cases: TC-SUP-030 (customers never see the internal fields), TC-SUP-031 (permission gating). |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database); migration V041 applied by hand.

## Test Cases in This Plan
| Test Case ID | Title | Acceptance criteria | Priority | Automated? | Status |
|---|---|---|---|---|---|
| [TC-SUP-021](TC-SUP-021.md) | A new incident is Logged, and the internal fields stay off the customer record | AC-1 | P0 | Yes | Passed |
| [TC-SUP-022](TC-SUP-022.md) | Incident phases: Resolved needs a summary, Closed only after Resolved and is final | AC-2 | P0 | Yes | Passed |
| [TC-SUP-023](TC-SUP-023.md) | An incident being investigated cannot have an end time | AC-3 | P1 | Yes | Passed |
| [TC-SUP-024](TC-SUP-024.md) | A resolved incident is reopened by removing its end time | AC-4 | P1 | Yes | Passed |
| [TC-SUP-025](TC-SUP-025.md) | Assignee and linked tickets | AC-5 | P1 | Yes | Passed |
| [TC-SUP-026](TC-SUP-026.md) | A problem links several incidents; an incident is in one problem only | AC-6 | P0 | Yes | Passed |
| [TC-SUP-027](TC-SUP-027.md) | Problem statuses: root cause before Resolved, every action done before Closed | AC-7 | P0 | Yes | Passed |
| [TC-SUP-028](TC-SUP-028.md) | A closed problem takes no more incidents; a problem needs a title | AC-8 | P1 | Yes | Passed |
| [TC-SUP-029](TC-SUP-029.md) | The problem list shows counts of incidents and open actions | AC-9 | P2 | Yes | Passed |
| [TC-SUP-030](TC-SUP-030.md) | Security: customers never see the internal incident fields | AC-10 | P0 | Yes | Passed |
| [TC-SUP-031](TC-SUP-031.md) | Security: only `MANAGE_SERVICE_STATUS` reaches incidents and problems | AC-11 | P0 | Yes | Passed |
| [TC-SUP-032](TC-SUP-032.md) | The Incidents tab: list, create, edit by phase, link tickets, closed read-only | AC-12 | P0 | Yes | Passed |
| [TC-SUP-033](TC-SUP-033.md) | The Problems tab: list, create, detail, blocked moves, actions, closed read-only | AC-13 | P0 | Yes | Passed |
| [TC-SUP-034](TC-SUP-034.md) | The Manage tab no longer posts incidents | AC-14 | P0 | Yes | Passed |
| [TC-SUP-035](TC-SUP-035.md) | Incident and problem changes are audited | AC-15 | P1 | No | Not Run |
| [TC-SUP-036](TC-SUP-036.md) | Migration V041: fresh load, upgrade and re-run; existing incidents get a phase | AC-16 | P1 | No | Partly passed |

## Entry Criteria
FRD Approved; migration V041 applied.

## Exit Criteria
- All automated cases passing; the manual cases run and passed before UAT sign-off.

## Blocked or partial
- **Not covered by an automated test:** requests to the incident and problem endpoints without the permission (TC-SUP-031), the audit entries (TC-SUP-035), the migration backfill (TC-SUP-036), and the JSON of `/me/service-status` for an incident with internal data (TC-SUP-030 asserts the record type).
- **Regression note:** the earlier status-page cases [TC-PRT-002](../service-status-page/TC-PRT-002.md) and [TC-PRT-004](../service-status-page/TC-PRT-004.md) now rely on the Incidents tab tests (the Manage tab no longer posts incidents).

## Risks
The engineering defaults of C101 are to be confirmed by the product owner.
