# TESTPLAN-PTR-001: Provider Onboarding Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-PTR-001 |
| Requirement(s) covered | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md). Out of scope: Publisher Management, Revenue Sharing, Partner Operations (see the FRD's own Out of scope).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new application and admin partner pages) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | The skip-a-stage and already-decided cases below (AC-3, AC-4) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-PTR-001](TC-PTR-001.md) | AC-1 | P0 | Yes |
| [TC-PTR-002](TC-PTR-002.md) | AC-2 | P0 | Yes |
| [TC-PTR-003](TC-PTR-003.md) | AC-3 | P0 | Yes |
| [TC-PTR-004](TC-PTR-004.md) | AC-4 | P0 | Yes |
| [TC-PTR-005](TC-PTR-005.md) | AC-5 | P0 | Yes |
| [TC-PTR-006](TC-PTR-006.md) | AC-6 | P0 | Yes |
