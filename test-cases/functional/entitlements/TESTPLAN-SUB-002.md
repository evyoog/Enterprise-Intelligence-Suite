# TESTPLAN-SUB-002: Entitlements

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-002 |
| Requirement(s) covered | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) (EN-1 to EN-5), [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) (RR-1 B: `accessMode`) |
| Status | Approved with the FRD (product owner, 2026-10-13) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md). Out of scope: consumption tracking and enforcement (each product does it), licence keys (dropped), granting or revoking by hand (entitlements are derived).

## Test Strategy
| Level | Approach |
|---|---|
| Unit / integration | `EntitlementServiceTest` (Spring Boot, H2); `EntitlementsAuthorizationTest` (MockMvc, security); `SeatsGraceAndRecipientsTest` (the read-only grace mode) |
| Frontend | Vitest and Testing Library with jest-axe: `MySubscriptionsPage.test.tsx`, `AdminEntitlementsPage.test.tsx`, API mocked |
| Manual | The cases marked Not Run or with a "Not covered" note; dev environment with a local PostgreSQL |

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-SUB-024](TC-SUB-024.md) | AC-1 features and only the limit | P0 | Yes |
| [TC-SUB-025](TC-SUB-025.md) | AC-2 each active subscription listed | P1 | Yes (partly) |
| [TC-SUB-026](TC-SUB-026.md) | AC-3 organization union | P1 | Yes (partly) |
| [TC-SUB-027](TC-SUB-027.md) | AC-4 suspended, cancelled, expired, pending grant nothing | P0 | Yes |
| [TC-SUB-028](TC-SUB-028.md) | AC-5 feature not in the plan | P0 | Yes |
| [TC-SUB-029](TC-SUB-029.md) | AC-6 internal check needs the secret | P0 | Yes |
| [TC-SUB-030](TC-SUB-030.md) | AC-7 only the caller's own entitlements | P0 | No |
| [TC-SUB-031](TC-SUB-031.md) | AC-8 admin lookup, audit, permission | P0 | Yes (partly) |
| [TC-SUB-032](TC-SUB-032.md) | AC-9 member access | P0 | Yes |
| [TC-SUB-033](TC-SUB-033.md) | AC-10 reasons and malformed body | P1 | Yes |
| [TC-SUB-034](TC-SUB-034.md) | AC-11 plan change at once | P1 | Yes |
| [TC-SUB-035](TC-SUB-035.md) | AC-12 past term end | P1 | Yes |
| [TC-SUB-036](TC-SUB-036.md) | AC-13 My subscriptions section | P1 | Yes |
| [TC-SUB-037](TC-SUB-037.md) | AC-14 admin lookup page | P1 | Yes |
| [TC-SUB-058](TC-SUB-058.md) | AC-15, AC-16 read-only access mode in the grace period | P0 | Yes (partly) |
