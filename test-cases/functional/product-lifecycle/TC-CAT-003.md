# TC-CAT-003: Product Lifecycle & Structure — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-003 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE product with an existing subscription.

## Steps
1. Arrange: create the product and subscribe to it.
2. Act: call retire.
3. Observe the public listing and the subscription.

## Expected Result
The product's status is RETIRED, it is absent from `GET /products`, and the existing subscription is unchanged.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `aRetiredProductIsExcludedFromThePublicStorefront`
- `backend/.../product/service/ProductServiceTest.java` — `publishAndRetireChangeStatusAndAreReversible`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
