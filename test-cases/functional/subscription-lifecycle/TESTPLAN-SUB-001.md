# TESTPLAN-SUB-001: Subscription Lifecycle Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-001 |
| Requirement(s) covered | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-27) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope (organization-owned subscriptions, quantity/scheduled changes, entitlements/licenses/quotas, billing).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new `/my/subscriptions` page) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | Ownership cases below (AC-10) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-SUB-001](TC-SUB-001.md) | AC-1 | P0 | Yes |
| [TC-SUB-002](TC-SUB-002.md) | AC-2 | P0 | Yes |
| [TC-SUB-003](TC-SUB-003.md) | AC-3 | P0 | Yes |
| [TC-SUB-004](TC-SUB-004.md) | AC-4 | P0 | Yes |
| [TC-SUB-005](TC-SUB-005.md) | AC-5 | P0 | Yes |
| [TC-SUB-006](TC-SUB-006.md) | AC-6 | P0 | Yes |
| [TC-SUB-007](TC-SUB-007.md) | AC-7 | P0 | Yes |
| [TC-SUB-008](TC-SUB-008.md) | AC-8 | P0 | Yes |
| [TC-SUB-009](TC-SUB-009.md) | AC-9 | P0 | Yes |
| [TC-SUB-010](TC-SUB-010.md) | AC-10 | P0 | Yes |
| [TC-SUB-011](TC-SUB-011.md) | AC-11 | P0 | Yes |
| [TC-SUB-012](TC-SUB-012.md) | AC-12 | P0 | Yes |
