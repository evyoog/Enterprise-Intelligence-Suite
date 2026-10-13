# TC-MKT-030: A signed-out visitor signs in first; the card is accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-030 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C93, REQ-MKT-003.10) |
| Acceptance Criterion | [AC-28](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Signed in (unless stated); a product with a free plan and two paid plans, a second product with a paid plan, and a product with only a free plan.

## Steps
1. Signed out, click **Add to cart**.
2. Run the accessibility check on the cards and notice.

## Expected Result
The visitor goes to `/login?returnTo=/cart?add={id}&plan={planId}` and no cart call is made; the product is added after sign-in. No axe violations.

## Automated coverage
- `frontend/src/components/cart/useAddToCart.test.tsx` — `sends a signed-out visitor to sign in…`, `has no detectable a11y violations`

## Actual Result
The automated test passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
