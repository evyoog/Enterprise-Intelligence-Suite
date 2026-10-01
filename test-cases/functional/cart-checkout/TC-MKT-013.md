# TC-MKT-013: The cart is stored on the server and is the same after sign-out or on another device

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-013 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A signed-in customer with two items in the cart.

## Steps
1. Sign out.
2. Sign in on another browser.
3. Open `/cart`.

## Expected Result
The same two items, plans and prices are shown.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — every test reads the cart back from the database through `getCart`
- Cross-device check: manual

## Actual Result
Server-side storage verified by the automated tests on 2026-10-01; the cross-device check has not been run manually yet.

## Status
Partly passed (manual step not run)

## Linked Defect (if failed)
None.
