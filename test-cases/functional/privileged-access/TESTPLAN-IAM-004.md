# TESTPLAN-IAM-004: Privileged Access (User and Organization Administrator) Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-004 |
| Requirement(s) covered | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-IAM-023](TC-IAM-023.md) | AC-1 | P0 | Yes |
| [TC-IAM-024](TC-IAM-024.md) | AC-2 | P0 | Yes |
| [TC-IAM-025](TC-IAM-025.md) | AC-3 | P0 | Yes |
| [TC-IAM-026](TC-IAM-026.md) | AC-4 | P0 | Partly |
| [TC-IAM-027](TC-IAM-027.md) | AC-5 | P0 | Partly |
| [TC-IAM-028](TC-IAM-028.md) | AC-6 | P0 | Partly |
| [TC-IAM-029](TC-IAM-029.md) | AC-7 | P0 | No |
| [TC-IAM-030](TC-IAM-030.md) | AC-8 | P0 | Yes |
| [TC-IAM-031](TC-IAM-031.md) | AC-9 | P0 | Yes |
| [TC-IAM-032](TC-IAM-032.md) | AC-10 | P0 | Yes |
| [TC-IAM-033](TC-IAM-033.md) | AC-11 | P0 | Yes |
| [TC-IAM-034](TC-IAM-034.md) | AC-12 | P0 | Yes |

## Entry Criteria
- FRD REQ-IAM-004 Approved (product owner, 2026-09-25).

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
