# TC-ORD-002: A plan from a different product is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-002 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two products, each with its own plan.

## Steps
1. Arrange: create product A and product B, each with a plan.
2. Act: submit an order for product A using product B's plan id.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `planMustBelongToTheOrderedProduct`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
