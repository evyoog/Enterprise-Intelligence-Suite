# TESTPLAN-SUP-001: Ticket Management Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUP-001 |
| Requirement(s) covered | [REQ-SUP-001](../../../docs/02-requirements/FRD/ticket-management/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/ticket-management/acceptance-criteria.md). Out of scope: AI-driven ticket handling, SLA policies, Incident & Problem records, reopening a resolved/closed ticket (see the FRD's own Out of scope).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new `/support/tickets` and `/admin/support/tickets` pages) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | Ownership case below (AC-2) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-SUP-001](TC-SUP-001.md) | AC-1 | P0 | Yes |
| [TC-SUP-002](TC-SUP-002.md) | AC-2 | P0 | Yes |
| [TC-SUP-003](TC-SUP-003.md) | AC-3 | P0 | Yes |
| [TC-SUP-004](TC-SUP-004.md) | AC-4 | P0 | Yes |
| [TC-SUP-005](TC-SUP-005.md) | AC-5, AC-6 | P0 | Yes |
