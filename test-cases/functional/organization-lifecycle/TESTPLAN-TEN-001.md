# TESTPLAN-TEN-001: Organization Lifecycle Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-TEN-001 |
| Requirement(s) covered | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library + jest-axe in `frontend/` |
| Integration | Spring Boot tests against H2 with an in-memory Keycloak fake in `backend/` |
| Security (SAST/DAST) | Permission cases in AC-9; self-lockout in AC-6; token cut-off in AC-8 |
| Performance | Not specified |
| Acceptance / UAT | Manual checks in each test case, in the dev environment against a real Keycloak test user |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-TEN-001](TC-TEN-001.md) | AC-1 | P0 | Yes |
| [TC-TEN-002](TC-TEN-002.md) | AC-2 | P0 | Yes |
| [TC-TEN-003](TC-TEN-003.md) | AC-3 | P0 | Yes |
| [TC-TEN-004](TC-TEN-004.md) | AC-4 | P0 | Yes |
| [TC-TEN-005](TC-TEN-005.md) | AC-5 | P0 | Yes |
| [TC-TEN-006](TC-TEN-006.md) | AC-6 | P0 | Yes |
| [TC-TEN-007](TC-TEN-007.md) | AC-7 | P0 | Yes |
| [TC-TEN-008](TC-TEN-008.md) | AC-8 | P0 | Yes |
| [TC-TEN-009](TC-TEN-009.md) | AC-9 | P0 | Yes |
| [TC-TEN-010](TC-TEN-010.md) | AC-10 | P0 | Partly |
