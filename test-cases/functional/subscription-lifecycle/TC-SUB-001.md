# TC-SUB-001: Subscription Lifecycle — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-001 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription owned by the caller.

## Steps
1. Arrange: subscribe a customer to a product (status ACTIVE).
2. Act: call suspendSubscription for that subscription.
3. Observe the returned status.

## Expected Result
Status becomes SUSPENDED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `suspendThenReactivateRoundTrips`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
