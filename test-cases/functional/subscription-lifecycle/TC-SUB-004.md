# TC-SUB-004: Subscription Lifecycle — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-004 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription owned by the caller.

## Steps
1. Arrange: an active subscription.
2. Act: call cancelSubscription.
3. Act: attempt cancelSubscription, reactivateSubscription and renewSubscription again on the same subscription.
4. Observe each response.

## Expected Result
Status becomes CANCELLED after step 2; every action in step 3 is refused (400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `cancelIsOneWay`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
