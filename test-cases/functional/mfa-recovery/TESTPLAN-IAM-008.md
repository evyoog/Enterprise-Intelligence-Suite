# TESTPLAN-IAM-008: MFA Recovery Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-008 |
| Requirement(s) covered | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library + jest-axe in `frontend/` |
| Integration | Spring Boot tests against H2 in `backend/` |
| Security (SAST/DAST) | Cross-organization and self-reset refusals (AC-2, AC-3) |
| Performance | Not specified |
| Acceptance / UAT | Manual checks in each test case, in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-IAM-050](TC-IAM-050.md) | AC-1 | P0 | Yes |
| [TC-IAM-051](TC-IAM-051.md) | AC-2 | P0 | Yes |
| [TC-IAM-052](TC-IAM-052.md) | AC-3 | P0 | Yes |
| [TC-IAM-053](TC-IAM-053.md) | AC-4 | P0 | Yes |
| [TC-IAM-054](TC-IAM-054.md) | AC-5 | P0 | Partly |

## Entry Criteria
FRD Approved.

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
