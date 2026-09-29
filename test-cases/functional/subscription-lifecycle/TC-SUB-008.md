# TC-SUB-008: Subscription Lifecycle — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-008 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription and a plan on the same product.

## Steps
1. Arrange: subscribe a customer to a product with a plan defined.
2. Act: call changePlan with that plan's id.
3. Observe the returned planId/planName.
4. Act: call changePlan with `null`.
5. Observe the returned planId.

## Expected Result
Step 3: planId/planName reflect the chosen plan. Step 5: planId is null (cleared).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `changePlanValidatesPlanBelongsToSameProduct`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
