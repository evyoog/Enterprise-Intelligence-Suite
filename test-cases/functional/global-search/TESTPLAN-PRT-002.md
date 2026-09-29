# TESTPLAN-PRT-002: Global Search Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-PRT-002 |
| Requirement(s) covered | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md). Out of scope: semantic search, search-history storage/UI (reused as-is from Phase 17), any dashboard wiring (see the FRD's own Out of scope).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new `/search` page) |
| Integration | Same backend test class, exercising the real `ProductService`/`KnowledgeArticleService`/`SupportTicketService` read paths |
| Security (SAST/DAST) | The signed-out-caller ticket-visibility case below (AC-3) |
| Performance | Not specified |
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
