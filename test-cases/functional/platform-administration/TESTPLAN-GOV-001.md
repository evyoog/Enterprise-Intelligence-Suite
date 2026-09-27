# TESTPLAN-GOV-001: Platform Administration Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-GOV-001 |
| Requirement(s) covered | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-27) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` where the UI changed) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | Permission cases below |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-GOV-001](TC-GOV-001.md) | AC-1 | P0 | Yes |
| [TC-GOV-002](TC-GOV-002.md) | AC-2 | P0 | Yes |
| [TC-GOV-003](TC-GOV-003.md) | AC-3 | P0 | Yes |
| [TC-GOV-004](TC-GOV-004.md) | AC-4 | P0 | Yes |
| [TC-GOV-005](TC-GOV-005.md) | AC-5 | P0 | Yes |
| [TC-GOV-006](TC-GOV-006.md) | AC-6 | P0 | Yes |
| [TC-GOV-007](TC-GOV-007.md) | AC-7 | P0 | Yes |
