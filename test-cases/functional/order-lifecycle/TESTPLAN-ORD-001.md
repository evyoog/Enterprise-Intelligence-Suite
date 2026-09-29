# TESTPLAN-ORD-001: Order Lifecycle Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-ORD-001 |
| Requirement(s) covered | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope (individual-customer orders, managing an already-provisioned org subscription, multi-level approval, any workflow engine).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new `/organization/orders` page) |
| Integration | Same backend test class, exercising the real service/repository layers, including provisioning via `SubscriptionService#subscribeOrganization` |
| Security (SAST/DAST) | Permission and cross-organization cases below (AC-4, AC-8) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-ORD-001](TC-ORD-001.md) | AC-1 | P0 | Yes |
| [TC-ORD-002](TC-ORD-002.md) | AC-2 | P0 | Yes |
| [TC-ORD-003](TC-ORD-003.md) | AC-3 | P0 | Yes |
| [TC-ORD-004](TC-ORD-004.md) | AC-4 | P0 | Yes |
| [TC-ORD-005](TC-ORD-005.md) | AC-5 | P0 | Yes |
| [TC-ORD-006](TC-ORD-006.md) | AC-6 | P0 | Yes |
| [TC-ORD-007](TC-ORD-007.md) | AC-7 | P0 | Yes |
| [TC-ORD-008](TC-ORD-008.md) | AC-8 | P0 | Yes |
