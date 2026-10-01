# TC-MKT-012: A signed-out visitor is asked to sign in before anything is added

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-012 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Not signed in.

## Steps
1. Click Buy on any plan.
2. Call `GET /api/me/cart` without a token.

## Expected Result
Step 1: the sign-in page opens with a return link to `/cart?add=…` (the item is added only after sign-in — engineering default for Open question 5). Step 2: 401.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/CartAuthorizationTest.java` — `theCartNeedsASignedInUser`
- `frontend/src/pages/ProductDetailPage.test.tsx` — "sends a signed-out visitor to sign in first, with a way back to the cart"
- `frontend/src/pages/HomePage.test.tsx` — subscribe button test

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
