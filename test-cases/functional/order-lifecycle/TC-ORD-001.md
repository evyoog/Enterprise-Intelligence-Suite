# TC-ORD-001: Member submits an order for a product with a plan

| Field | Value |
|---|---|
| Test Case ID (required) | TC-ORD-001 |
| Requirement ID (required) | [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/order-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-ORD-001](TESTPLAN-ORD-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active member of an organization; an ACTIVE product with a plan.

## Steps
1. Arrange: add an admin and a member to an organization; create a product with a plan.
2. Act: the member calls submitOrder with the product and plan ids.
3. Observe the returned status.

## Expected Result
The order is created with status SUBMITTED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/OrderServiceTest.java` — `memberSubmitsOrderThenAdminApprovesAndProvisions`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
