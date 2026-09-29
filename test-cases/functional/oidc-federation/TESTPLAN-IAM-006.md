# TESTPLAN-IAM-006: OIDC Identity-Provider Federation Test Plan

| Field | Value |
|---|---|
| Feature ID (required) | FTR-IAM-006 |
| Requirement(s) covered | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Author | Not specified |
| Status | Approved with the FRD (product owner, 2026-09-25/26) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md).

## Test Strategy
| Level | Approach |
|---|---|
| Unit | Vitest + Testing Library + jest-axe in `frontend/` |
| Integration | Spring Boot tests against H2 with an in-memory identity provider that signs real RS256 ID tokens |
| Security (SAST/DAST) | Token tampering, state replay and organization binding (AC-6, AC-7) |
| Performance | Not specified |
| Acceptance / UAT | End-to-end sign-in against a real test tenant (AC-5 manual check) |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database) and a test identity provider tenant.

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-IAM-055](TC-IAM-055.md) | AC-1 | P0 | Yes |
| [TC-IAM-056](TC-IAM-056.md) | AC-2 | P0 | Yes |
| [TC-IAM-057](TC-IAM-057.md) | AC-3 | P0 | Yes |
| [TC-IAM-058](TC-IAM-058.md) | AC-4 | P0 | Yes |
| [TC-IAM-059](TC-IAM-059.md) | AC-5 | P0 | Partly |
| [TC-IAM-060](TC-IAM-060.md) | AC-6 | P0 | Yes |
| [TC-IAM-061](TC-IAM-061.md) | AC-7 | P0 | Yes |
| [TC-IAM-062](TC-IAM-062.md) | AC-8 | P0 | Yes |
| [TC-IAM-063](TC-IAM-063.md) | AC-9 | P0 | Partly |

## Entry Criteria
FRD Approved.

## Exit Criteria
- All automated cases passing; manual checks run and passed before UAT sign-off.

## Risks
The HTTP client (`HttpOidcProviderClient`) is not exercised by automated tests; the AC-5 manual check covers it.
