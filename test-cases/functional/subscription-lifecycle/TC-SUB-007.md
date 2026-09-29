# TC-SUB-007: Subscription Lifecycle — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-007 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription with no plan and no prior expiresAt.

## Steps
1. Arrange: subscribe a customer to a product (no plan set).
2. Act: call renewSubscription.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `renewRefusedWithNoBillingPeriodToRenew`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
