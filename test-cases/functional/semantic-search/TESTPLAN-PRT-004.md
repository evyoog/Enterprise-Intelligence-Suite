# TESTPLAN-PRT-004: Semantic Search Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-PRT-003 |
| Requirement(s) covered | [REQ-PRT-003](../../../docs/02-requirements/FRD/semantic-search/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-10-05, C70) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/semantic-search/acceptance-criteria.md). Out of scope: the quality of a real embedding model (no model could be downloaded; see the FRD), AI-written answers.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | `SearchTextUtilitiesTest` (chunking, hybrid merge); `ai-service/tests/test_embed.py` |
| Integration (PostgreSQL + pgvector) | `PostgresSearchIntegrationTest` with `StubEmbeddingServer` (an in-process copy of the ai-service stub provider over HTTP) |
| Security | TC-PRT-030 private records never embedded; TC-PRT-020 (global search plan) organization isolation in hybrid mode |
| Performance | TC-PRT-027 hybrid p95 < 800 ms (stub model; a real model's own time not measured) |
| Acceptance / UAT | Manual check with a real model once one is chosen: **Not specified** |

## Environments
Local and CI: PostgreSQL 16 with pgvector 0.5+; ai-service tests in Python 3.11+.

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-PRT-028](TC-PRT-028.md) | AC-1 | P0 | Yes |
| [TC-PRT-029](TC-PRT-029.md) | AC-2 | P0 | Yes |
| [TC-PRT-030](TC-PRT-030.md) | AC-3 | P0 | Yes |
| [TC-PRT-031](TC-PRT-031.md) | AC-4 | P0 | Yes |
| [TC-PRT-027](../global-search/TC-PRT-027.md) | AC-5 | P0 | Yes |
