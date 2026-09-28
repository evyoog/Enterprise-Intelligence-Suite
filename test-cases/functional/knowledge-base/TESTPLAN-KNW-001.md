# TESTPLAN-KNW-001: Knowledge Base Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-KNW-001 |
| Requirement(s) covered | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-28) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md). Out of scope: 11.01.02 AI Knowledge (no vector store/embeddings decision exists), rich content, categories/tags, full version history.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` for the new `/knowledge-base` and `/admin/knowledge-base` pages) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | The public-vs-draft visibility boundary (AC-1, AC-3) |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-KNW-001](TC-KNW-001.md) | AC-1 | P0 | Yes |
| [TC-KNW-002](TC-KNW-002.md) | AC-2 | P0 | Yes |
| [TC-KNW-003](TC-KNW-003.md) | AC-3 | P0 | Yes |
| [TC-KNW-004](TC-KNW-004.md) | AC-4 | P0 | Yes |
| [TC-KNW-005](TC-KNW-005.md) | AC-5 | P0 | Yes |
| [TC-KNW-006](TC-KNW-006.md) | AC-6 | P0 | Yes |
