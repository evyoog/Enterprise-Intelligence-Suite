# TESTPLAN-IAM-007: Claim Mapping Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-007 |
| Requirement(s) covered | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25/26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library + jest-axe in `frontend/` |
| Integration | Spring Boot tests with real signed SAML responses and an in-memory OIDC provider |
| Security (SAST/DAST) | No role is ever taken from claims (BR-IAM-007.5) |
| Performance | Not specified |
| Acceptance / UAT | Manual checks in each test case |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-IAM-064](TC-IAM-064.md) | AC-1 | P0 | Yes |
| [TC-IAM-065](TC-IAM-065.md) | AC-2 | P0 | Yes |
| [TC-IAM-066](TC-IAM-066.md) | AC-3 | P0 | Yes |
| [TC-IAM-067](TC-IAM-067.md) | AC-4 | P0 | Yes |
| [TC-IAM-068](TC-IAM-068.md) | AC-5 | P0 | Yes |
| [TC-IAM-069](TC-IAM-069.md) | AC-6 | P0 | Partly |

## Entry Criteria
FRD Approved.

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
Not specified.
