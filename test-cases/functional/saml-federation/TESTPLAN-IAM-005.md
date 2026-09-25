# TESTPLAN-IAM-005: SAML Federation — Edit Provider Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-005 |
| Requirement(s) covered | [REQ-IAM-005](../../../docs/02-requirements/FRD/saml-federation/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/saml-federation/acceptance-criteria.md). Out of scope: what the FRD lists as out of scope.

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
| [TC-IAM-035](TC-IAM-035.md) | AC-1 | P0 | Partly |
| [TC-IAM-036](TC-IAM-036.md) | AC-2 | P0 | Partly |
| [TC-IAM-037](TC-IAM-037.md) | AC-3 | P0 | Partly |
| [TC-IAM-038](TC-IAM-038.md) | AC-4 | P0 | No |
| [TC-IAM-039](TC-IAM-039.md) | AC-5 | P0 | No |
| [TC-IAM-040](TC-IAM-040.md) | AC-6 | P0 | Yes |
| [TC-IAM-041](TC-IAM-041.md) | AC-7 | P0 | Yes |

## Entry Criteria
- FRD REQ-IAM-005 Approved (product owner, 2026-09-25).

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
