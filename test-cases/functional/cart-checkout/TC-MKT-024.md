# TC-MKT-024: Cart changes are not audited; order submission and payment are

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-024 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P3 |
| Type | Functional |
| Automated | No |

## Preconditions
A signed-in customer.

## Steps
1. Add, change and remove items.
2. Check the audit log.
3. Submit the cart.

## Expected Result
No audit entries for the cart changes; `SUBSCRIPTION_CREATED`/`INVOICE_GENERATED` (individual) or `ORDER_SUBMITTED` (organization) entries after submitting.

## Automated coverage
- Code review: `CartService` writes no audit entries; the reused subscribe, invoice and order services audit as before

## Actual Result
Not run manually yet.

## Status
Not run

## Linked Defect (if failed)
None.
