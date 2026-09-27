# TC-SUB-002: Subscription Lifecycle — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-002 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SUSPENDED subscription owned by the caller.

## Steps
1. Arrange: suspend an active subscription.
2. Act: call suspendSubscription again on the same subscription.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `suspendThenReactivateRoundTrips`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
