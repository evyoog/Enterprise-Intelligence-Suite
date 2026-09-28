# TESTPLAN-MKT-002: Product Reviews & Ratings Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-002 |
| Requirement(s) covered | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md). Out of scope: AI-assisted moderation, a cached average-rating column, replying to a review (see the FRD's own Out of scope).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new product detail and admin moderation pages) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | The already-decided-review re-moderation case below (AC-4) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-MKT-005](TC-MKT-005.md) | AC-1 | P0 | Yes |
| [TC-MKT-006](TC-MKT-006.md) | AC-2 | P0 | Yes |
| [TC-MKT-007](TC-MKT-007.md) | AC-3 | P0 | Yes |
| [TC-MKT-008](TC-MKT-008.md) | AC-4 | P0 | Yes |
| [TC-MKT-009](TC-MKT-009.md) | AC-5 | P0 | Yes |
| [TC-MKT-010](TC-MKT-010.md) | AC-6 | P0 | Yes |
