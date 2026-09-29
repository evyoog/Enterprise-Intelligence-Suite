# TESTPLAN-TEN-004: Tenant Lifecycle Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-TEN-004 |
| Requirement(s) covered | [REQ-TEN-004](../../../docs/02-requirements/FRD/tenant-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-27) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/tenant-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-TEN-023](TC-TEN-023.md) | AC-1 | P0 | Yes |
| [TC-TEN-024](TC-TEN-024.md) | AC-2 | P0 | Yes |
| [TC-TEN-025](TC-TEN-025.md) | AC-3 | P0 | Yes |
| [TC-TEN-026](TC-TEN-026.md) | AC-4 | P0 | Yes |
