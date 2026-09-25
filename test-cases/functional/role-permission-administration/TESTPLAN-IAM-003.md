# TESTPLAN-IAM-003: Role and Permission Administration Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-003 |
| Requirement(s) covered | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-IAM-013](TC-IAM-013.md) | AC-1 | P0 | Partly |
| [TC-IAM-014](TC-IAM-014.md) | AC-2 | P0 | Yes |
| [TC-IAM-015](TC-IAM-015.md) | AC-3 | P0 | Partly |
| [TC-IAM-016](TC-IAM-016.md) | AC-4 | P0 | Yes |
| [TC-IAM-017](TC-IAM-017.md) | AC-5 | P0 | Yes |
| [TC-IAM-018](TC-IAM-018.md) | AC-6 | P0 | Partly |
| [TC-IAM-019](TC-IAM-019.md) | AC-7 | P0 | Yes |
| [TC-IAM-020](TC-IAM-020.md) | AC-8 | P0 | Yes |
| [TC-IAM-021](TC-IAM-021.md) | AC-9 | P0 | Yes |
| [TC-IAM-022](TC-IAM-022.md) | AC-10 | P0 | Yes |

## Entry Criteria
- FRD REQ-IAM-003 Approved (product owner, 2026-09-25).

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
