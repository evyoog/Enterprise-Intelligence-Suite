# TESTPLAN-PRT-002: Global Search Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-PRT-002 |
| Requirement(s) covered | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28); C70 cases added 2026-10-05 |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md). Semantic search has its own plan, [TESTPLAN-PRT-004](../semantic-search/TESTPLAN-PRT-004.md). Out of scope: search-history storage (reused from Phase 17), any dashboard wiring.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/`; pure unit tests (`SearchTextUtilitiesTest`); Vitest + Testing Library + jest-axe in `frontend/` |
| Integration (PostgreSQL) | `PostgresSearchIntegrationTest`, `PostgresGlobalSearchServiceTest` — run when `EIS_PG_TEST_URL` is set (CI: PostgreSQL 16 + pgvector service) |
| Integration | Same backend test class, exercising the real `ProductService`/`KnowledgeArticleService`/`SupportTicketService` read paths |
| Security (SAST/DAST) | The signed-out-caller ticket-visibility case below (AC-3) |
| Performance | TC-PRT-027: quality test set and speed (keyword p95 < 500 ms) on PostgreSQL with 13,000 records |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-PRT-001](TC-PRT-001.md) | AC-1 | P0 | Yes |
| [TC-PRT-002](TC-PRT-002.md) | AC-2 | P0 | Yes |
| [TC-PRT-003](TC-PRT-003.md) | AC-3 | P0 | Yes |
| [TC-PRT-004](TC-PRT-004.md) | AC-4 | P0 | Yes |
| [TC-PRT-020](TC-PRT-020.md) | AC-5 | P0 | Yes |
| [TC-PRT-021](TC-PRT-021.md) | AC-6 | P0 | Yes |
| [TC-PRT-022](TC-PRT-022.md) | AC-7 | P0 | Yes |
| [TC-PRT-023](TC-PRT-023.md) | AC-8 | P0 | Yes |
| [TC-PRT-024](TC-PRT-024.md) | AC-9 | P0 | Yes |
| [TC-PRT-025](TC-PRT-025.md) | AC-10 | P0 | Yes |
| [TC-PRT-026](TC-PRT-026.md) | AC-11 | P0 | Yes |
| [TC-PRT-027](TC-PRT-027.md) | AC-12 | P0 | Yes |
