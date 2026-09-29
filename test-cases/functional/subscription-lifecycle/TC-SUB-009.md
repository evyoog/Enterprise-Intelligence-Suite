# TC-SUB-009: Subscription Lifecycle — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-009 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription on product A and a plan that belongs to product B.

## Steps
1. Arrange: subscribe to product A; create a plan on product B.
2. Act: call changePlan with product B's plan id.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `changePlanValidatesPlanBelongsToSameProduct`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
