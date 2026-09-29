# TESTPLAN-CAT-002: Plan Management Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-CAT-002 |
| Requirement(s) covered | [REQ-CAT-002](../../../docs/02-requirements/FRD/plan-management/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/plan-management/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-CAT-008](TC-CAT-008.md) | AC-1 | P0 | Yes |
| [TC-CAT-009](TC-CAT-009.md) | AC-2 | P0 | Yes |
| [TC-CAT-010](TC-CAT-010.md) | AC-3 | P0 | Yes |
