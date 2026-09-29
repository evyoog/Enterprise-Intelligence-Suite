# TC-SUB-012: Subscription Lifecycle — AC-12

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-012 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription with expiresAt in the future.

## Steps
1. Arrange: subscribe a customer, then set expiresAt to tomorrow directly.
2. Act: call expireOverdueSubscriptions.
3. Observe the subscription's status afterward.

## Expected Result
Status remains ACTIVE.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `expiryJobFlipsOverdueActiveSubscriptionsOnly`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
