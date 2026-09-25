# TESTPLAN-IAM-002: Member Role Assignment Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-002 |
| Requirement(s) covered | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library component tests in `frontend/` |
| Integration | Spring Boot tests against H2 in `backend/` (`mvn -B verify`) |
| Security (SAST/DAST) | Permission and tenant-isolation cases below; CI security gates when they are configured |
| Performance | Not specified |
| Acceptance / UAT | Manual checks listed in each test case, in the dev environment |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-IAM-007](TC-IAM-007.md) | AC-1 | P0 | Yes |
| [TC-IAM-008](TC-IAM-008.md) | AC-2 | P0 | Partly |
| [TC-IAM-009](TC-IAM-009.md) | AC-3 | P0 | Yes |
| [TC-IAM-010](TC-IAM-010.md) | AC-4 | P0 | Yes |
| [TC-IAM-011](TC-IAM-011.md) | AC-5 | P0 | Yes |
| [TC-IAM-012](TC-IAM-012.md) | AC-6 | P0 | Yes |

## Entry Criteria
- FRD REQ-IAM-002 Approved (product owner, 2026-09-25).

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
