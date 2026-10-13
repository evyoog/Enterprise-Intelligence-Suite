# TESTPLAN-GOV-001: Platform Administration Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-GOV-001 |
| Requirement(s) covered | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (REQ-GOV-001.1 to .6; .7 to .12 added by [C92](../../../docs/01-business/roadmap/open-decisions.md#c92)) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-27) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Spring Boot tests against H2 in `backend/` (Vitest + Testing Library in `frontend/` where the UI changed) |
| Integration | Same backend test class, exercising the real service/repository layers |
| Security (SAST/DAST) | Permission cases below |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-GOV-001](TC-GOV-001.md) | AC-1 | P0 | Yes |
| [TC-GOV-002](TC-GOV-002.md) | AC-2 | P0 | Yes |
| [TC-GOV-003](TC-GOV-003.md) | AC-3 | P0 | Yes |
| [TC-GOV-004](TC-GOV-004.md) | AC-4 | P0 | Yes |
| [TC-GOV-005](TC-GOV-005.md) | AC-5 | P0 | Yes |
| [TC-GOV-006](TC-GOV-006.md) | AC-6 | P0 | Yes |
| [TC-GOV-007](TC-GOV-007.md) | AC-7 | P0 | Yes |
| [TC-GOV-008](TC-GOV-008.md) | AC-8 built-in text and language | P0 | Yes |
| [TC-GOV-009](TC-GOV-009.md) | AC-9 edit and reset, audited | P0 | Yes |
| [TC-GOV-010](TC-GOV-010.md) | AC-10 placeholder and length rules | P0 | Yes |
| [TC-GOV-011](TC-GOV-011.md) | AC-11 preview | P1 | Yes |
| [TC-GOV-012](TC-GOV-012.md) | AC-12 edited text is sent | P0 | Yes |
| [TC-GOV-013](TC-GOV-013.md) | AC-13 language of a message | P0 | Yes |
| [TC-GOV-014](TC-GOV-014.md) | AC-14 platform defaults | P0 | Yes |
| [TC-GOV-015](TC-GOV-015.md) | AC-15 defaults reach preferences | P0 | Yes |
| [TC-GOV-016](TC-GOV-016.md) | AC-16 permission | P0 | Yes |
| [TC-GOV-017](TC-GOV-017.md) | AC-17 defaults screen | P1 | Yes |
| [TC-GOV-018](TC-GOV-018.md) | AC-18 templates screen | P1 | Yes |
