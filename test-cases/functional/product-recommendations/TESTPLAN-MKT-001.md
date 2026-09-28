# TESTPLAN-MKT-001: Product Recommendations Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-001 |
| Requirement(s) covered | [REQ-MKT-001](../../../docs/02-requirements/FRD/product-recommendations/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/product-recommendations/acceptance-criteria.md). Out of scope: any AI/ML-based recommendation model, personalization by purchase history (see the FRD's own Out of scope).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` |
| Integration | Same test class, exercising the real service/repository layers (real `ProductUsage` aggregation) |
| Security (SAST/DAST) | Not applicable — public, read-only data |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-MKT-001](TC-MKT-001.md) | AC-1 | P0 | Yes |
| [TC-MKT-002](TC-MKT-002.md) | AC-2 | P0 | Yes |
| [TC-MKT-003](TC-MKT-003.md) | AC-3 | P0 | Yes |
| [TC-MKT-004](TC-MKT-004.md) | AC-4 | P0 | Yes |
