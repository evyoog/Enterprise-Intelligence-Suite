# TESTPLAN-TEN-002: Member Lifecycle & Access Review Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-TEN-002 |
| Requirement(s) covered | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` where the UI changed) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | Permission and cross-organization cases below |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-TEN-011](TC-TEN-011.md) | AC-1 | P0 | Yes |
| [TC-TEN-012](TC-TEN-012.md) | AC-2 | P0 | Yes |
| [TC-TEN-013](TC-TEN-013.md) | AC-3 | P0 | Yes |
| [TC-TEN-014](TC-TEN-014.md) | AC-4 | P0 | Yes |
| [TC-TEN-015](TC-TEN-015.md) | AC-5 | P0 | Yes |
| [TC-TEN-016](TC-TEN-016.md) | AC-6 | P0 | Yes |
