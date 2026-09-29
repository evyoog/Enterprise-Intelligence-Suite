# TESTPLAN-TEN-003: Group Management Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-TEN-003 |
| Requirement(s) covered | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-TEN-017](TC-TEN-017.md) | AC-1 | P0 | Yes |
| [TC-TEN-018](TC-TEN-018.md) | AC-2 | P0 | Yes |
| [TC-TEN-019](TC-TEN-019.md) | AC-3 | P0 | Yes |
| [TC-TEN-020](TC-TEN-020.md) | AC-4 | P0 | Yes |
| [TC-TEN-021](TC-TEN-021.md) | AC-5 | P0 | Yes |
| [TC-TEN-022](TC-TEN-022.md) | AC-6 | P0 | Yes |
