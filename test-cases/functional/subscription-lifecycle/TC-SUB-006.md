# TC-SUB-006: Subscription Lifecycle — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-006 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An EXPIRED subscription with a MONTHLY plan and a past expiresAt.

## Steps
1. Arrange: subscribe, set the plan, then set status=EXPIRED and expiresAt in the past directly.
2. Act: call renewSubscription.
3. Observe the returned status and expiresAt.

## Expected Result
Status becomes ACTIVE and `expiresAt` is now in the future.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `renewExtendsFromMonthlyPlanAndReactivatesExpired`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
