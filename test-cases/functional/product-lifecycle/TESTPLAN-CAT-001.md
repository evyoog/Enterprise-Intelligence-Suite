# TESTPLAN-CAT-001: Product Lifecycle & Structure Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-CAT-001 |
| Requirement(s) covered | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-CAT-001](TC-CAT-001.md) | AC-1 | P0 | Yes |
| [TC-CAT-002](TC-CAT-002.md) | AC-2 | P0 | Yes |
| [TC-CAT-003](TC-CAT-003.md) | AC-3 | P0 | Yes |
| [TC-CAT-004](TC-CAT-004.md) | AC-4 | P0 | Yes |
| [TC-CAT-005](TC-CAT-005.md) | AC-5 | P0 | Yes |
| [TC-CAT-006](TC-CAT-006.md) | AC-6 | P0 | Yes |
| [TC-CAT-007](TC-CAT-007.md) | AC-7 | P0 | Yes |
