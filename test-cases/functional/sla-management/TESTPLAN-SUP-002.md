# TESTPLAN-SUP-002: SLA management

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUP-002 |
| Requirement(s) covered | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Author | Not specified |
| Status | Draft (the FRD is Approved, 2026-10-14; built 2026-10-14; engineering defaults to confirm) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md): TC-SUP-006 to TC-SUP-020. Out of scope (Not specified): SLA by plan, public holidays, pausing the clock, an on-call chain.

## Test Strategy
| Level | Approach |
|---|---|
| Unit / integration | Backend: `SlaCalculatorTest` (pure business-hours arithmetic), `SlaServiceTest` (deadlines, first response, warnings, breaches, settings; the scheduled job is off in tests and the check is called with a later "now"), `SupportTicketServiceTest` (regression). Frontend: `AdminSlaSettingsPage.test.tsx`, `SlaChip.test.tsx`, `AdminSupportTicketsPage.test.tsx` and `MyTicketsPage.test.tsx` with jest-axe. Manual: permissions of `/admin/support/sla`, audit entries, targets not moving existing tickets, tickets without deadlines. |
| Security (SAST/DAST) | Security case: TC-SUP-018 (only `MANAGE_SUPPORT_TICKETS` reads or changes the settings); TC-SUP-015 (customers never receive the SLA states). |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database); migration V041 applied by hand.

## Test Cases in This Plan
| Test Case ID | Title | Acceptance criteria | Priority | Automated? | Status |
|---|---|---|---|---|---|
| [TC-SUP-006](TC-SUP-006.md) | A new ticket gets both deadlines from its creation time | AC-1 | P0 | Yes | Passed |
| [TC-SUP-007](TC-SUP-007.md) | Business-hours arithmetic: Monday to Friday 09:00 to 18:00, Asia/Kolkata | AC-2 | P0 | Yes | Passed |
| [TC-SUP-008](TC-SUP-008.md) | A priority change recalculates both deadlines from the creation time | AC-3 | P0 | Yes | Passed |
| [TC-SUP-009](TC-SUP-009.md) | First response: the first time a person takes the ticket out of Open | AC-4 | P0 | Yes | Passed |
| [TC-SUP-010](TC-SUP-010.md) | A warning goes out once at 80% to the assignee and the alert list | AC-5 | P0 | Yes | Passed |
| [TC-SUP-011](TC-SUP-011.md) | A breach escalates the ticket, keeps the priority and flags it | AC-6 | P0 | Yes | Passed |
| [TC-SUP-012](TC-SUP-012.md) | A resolved ticket is no longer watched | AC-7 | P1 | Yes | Passed |
| [TC-SUP-013](TC-SUP-013.md) | Targets and business hours are editable and validated; the settings screen | AC-8, AC-16 | P0 | Yes | Passed |
| [TC-SUP-014](TC-SUP-014.md) | New targets do not move the deadlines of existing tickets | AC-9 | P1 | No | Not Run |
| [TC-SUP-015](TC-SUP-015.md) | Customers see the deadlines but never the SLA states | AC-10, AC-16 | P0 | Yes | Passed |
| [TC-SUP-016](TC-SUP-016.md) | Administrators see SLA chips, the breach flag and the SLA filter | AC-11, AC-16 | P0 | Yes | Passed |
| [TC-SUP-017](TC-SUP-017.md) | A ticket created before SLA tracking has no deadlines | AC-12 | P1 | Partly | Partly passed |
| [TC-SUP-018](TC-SUP-018.md) | Security: only `MANAGE_SUPPORT_TICKETS` reads or changes the SLA settings | AC-13 | P0 | Yes | Passed |
| [TC-SUP-019](TC-SUP-019.md) | SLA changes, warnings and breaches are audited | AC-14 | P1 | No | Not Run |
| [TC-SUP-020](TC-SUP-020.md) | Regression: the ticket actions of REQ-SUP-001 still work with the SLA fields | AC-15 | P0 | Yes | Passed |

## Entry Criteria
FRD Approved; migration V041 applied.

## Exit Criteria
- All automated cases passing; the manual cases run and passed before UAT sign-off.

## Blocked or partial
- **Not covered by an automated test:** targets not moving existing tickets (TC-SUP-014), the backend behaviour for a ticket without deadlines (TC-SUP-017), permissions of the SLA endpoints (TC-SUP-018), audit entries (TC-SUP-019), recalculation by escalation, and an alert address with no account (email only).
- Business hours are tested for Asia/Kolkata only; the date of the run does not matter because the service tests use an Urgent (24x7) ticket.

## Risks
The engineering defaults of C101 are to be confirmed by the product owner.
