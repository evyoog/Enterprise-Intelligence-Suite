# TESTPLAN-PRT-001: Interim Service Status Page Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-PRT-001 |
| Requirement(s) covered | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25/26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library + jest-axe in `frontend/` |
| Integration | Spring Boot tests against H2 in `backend/` |
| Security (SAST/DAST) | Permission cases in AC-5; purchased-only incident details in AC-2 |
| Performance | Not specified |
| Acceptance / UAT | Manual checks in each test case, in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-PRT-001](TC-PRT-001.md) | AC-1 | P0 | Yes |
| [TC-PRT-002](TC-PRT-002.md) | AC-2 | P0 | Yes |
| [TC-PRT-003](TC-PRT-003.md) | AC-3 | P0 | Yes |
| [TC-PRT-004](TC-PRT-004.md) | AC-4 | P0 | Yes |
| [TC-PRT-005](TC-PRT-005.md) | AC-5 | P0 | Yes |
| [TC-PRT-006](TC-PRT-006.md) | AC-6 | P0 | Yes |
| [TC-PRT-007](TC-PRT-007.md) | AC-7 | P0 | Partly |

## Entry Criteria
FRD Approved.

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
