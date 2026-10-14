# TESTPLAN-MKT-003: Cart and checkout

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-003 |
| Requirement(s) covered | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) |
| Decision | [C59](../../../docs/01-business/roadmap/open-decisions.md#c59), [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) (CA-1 to CA-7, PB-1 coupon) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-01 at the product owner's request with the engineering defaults recorded under C59, changed 2026-10-14 for C100) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md): TC-MKT-011 to TC-MKT-026 for C93 (Add to cart on the card), TC-MKT-027 to TC-MKT-030, and for C100 (organization buying, purchase requests, one invoice, seats × price, the coupon field) TC-MKT-040 to TC-MKT-048. The redesigned checkout screen is covered in [TESTPLAN-BIL-001](../billing-payments/TESTPLAN-BIL-001.md) (TC-BIL-019 to TC-BIL-023).

## Approach
Backend: `CartServiceTest`, `CouponAndPurchaseTest` (coupons, purchase requests, direct purchase) and `CartAuthorizationTest` against the H2-backed Spring context (Razorpay mocked). Frontend: Vitest with Testing Library and jest-axe, API mocked. Manual: cross-device persistence (TC-MKT-013), the audit check (TC-MKT-024) and the cart that never expires (TC-MKT-047, Not Run).

## Test cases added by C100
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-MKT-040](TC-MKT-040.md) | An organization administrator or Manage billing holder buys directly with one invoice | AC-29 | Yes |
| [TC-MKT-041](TC-MKT-041.md) | A member's cart becomes one multi-item purchase request with a reason; approval creates one invoice | AC-30 | Yes |
| [TC-MKT-042](TC-MKT-042.md) | A changed price is confirmed once for the whole cart | AC-31 | Yes |
| [TC-MKT-043](TC-MKT-043.md) | An organization item shows seats × price | AC-32 | Yes |
| [TC-MKT-044](TC-MKT-044.md) | A coupon code is applied in the cart, shown as a discount and removed | AC-33 | Yes |
| [TC-MKT-045](TC-MKT-045.md) | A coupon that stops working before checkout blocks it and creates nothing | AC-34 | Yes |
| [TC-MKT-046](TC-MKT-046.md) | A purchase request carries no coupon | AC-35 | Yes |
| [TC-MKT-047](TC-MKT-047.md) | Cart items never expire; a visitor's chosen item is added after sign-in | AC-36 | No (Not Run) |
| [TC-MKT-048](TC-MKT-048.md) | The coupon field, its messages and the purchase-request dialog are accessible | AC-37 | Yes |

TC-MKT-023 (the member's request) and TC-MKT-017 (no quantity, fee or shipping) were updated on 2026-10-14 for C100. The coupon's invoice side is in [TESTPLAN-BIL-001](../billing-payments/TESTPLAN-BIL-001.md) (TC-BIL-119 to TC-BIL-124).

## Blocked or partial
- AC-6 tax line ("GST (18 %)"): needs the REQ-BIL-002 tax engine, not built yet.
