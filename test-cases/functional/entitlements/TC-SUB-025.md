# TC-SUB-025: Each active subscription lists product, plan, status, term end, features and limit

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-025 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.2) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with two ACTIVE subscriptions to different products.

## Steps
1. Read the customer's entitlements (`GET /me/entitlements`).

## Expected Result
Each subscription appears once with its product, plan, status, term end, included features and usage limit.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anActiveSubscriptionGivesTheFeaturesAndOnlyTheLimit` (the fields of one entitlement)
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "lists what the active plans include, read-only (C97 EN-1)"

Not covered by an automated test: a customer with two active subscriptions in one list; run it manually in the dev environment.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend test passed on 2026-10-13 (`vitest run`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
