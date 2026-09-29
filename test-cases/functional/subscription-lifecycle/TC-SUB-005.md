# TC-SUB-005: Subscription Lifecycle — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-005 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription on a MONTHLY plan.

## Steps
1. Arrange: subscribe, then change the plan to a MONTHLY plan.
2. Act: call renewSubscription.
3. Observe the returned expiresAt.

## Expected Result
`expiresAt` extends by 30 days from the later of now or the current `expiresAt`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `renewExtendsFromMonthlyPlanAndReactivatesExpired`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
