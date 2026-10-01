# REQ-MKT-003 — Cart and checkout

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. The open questions marked **Blocks approval** must be answered first.
**Decision:** [C59](../../../01-business/roadmap/open-decisions.md#c59) (cart, answer to D17). Related: [C55](../../../01-business/roadmap/open-decisions.md#c55) (checkout, Pay by invoice), [C50](../../../01-business/roadmap/open-decisions.md#c50) (no coupons or promotions), [C51](../../../01-business/roadmap/open-decisions.md#c51) (tax, REQ-BIL-002).

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md), pulled forward from [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) by [C59](../../../01-business/roadmap/open-decisions.md#c59) |
| Requirement ID | REQ-MKT-003 |
| Application | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md) |
| Application code | `APP-MKT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 03.03.01.01 | Select product | Yes: Buy adds the product to the cart (.1) |
| 03.03.01.02 | Select plan | Yes: Buy picks the plan, and Change plan changes it (.1, .4) |
| 03.03.01.03 | Configure options | No. No product-options model exists or is specified ([C40](../../../01-business/roadmap/open-decisions.md#c40)) |
| 03.03.01.04 | Apply discount | No. Coupons and promotions are excluded ([C50](../../../01-business/roadmap/open-decisions.md#c50)) |
| 03.03.01.05 | Accept terms | Yes, through the checkout consent checkbox ([REQ-BIL-001.18](../billing-payments/requirement.md)) |
| 03.03.01.06 | Submit order | Yes: Proceed to checkout or Submit order for approval (.8) |
| 03.03.02.01 | Validate eligibility | Yes: published product, existing active subscription (.5) |
| 03.03.02.02 | Validate payment | No. Payment verification is part of [REQ-BIL-001](../billing-payments/requirement.md) (.5–.8) |
| 03.03.02.03 | Validate dependencies | Yes: required products (.5), using the dependencies from [REQ-CAT-001.6](../product-lifecycle/requirement.md) |

## Summary
A signed-in customer collects paid plans in a server-side cart, reviews and edits them, and takes them to checkout. **Buy** on any paid plan adds the plan to the cart and opens `/cart`. The cart re-validates every item before payment. An individual's cart continues to billing details and payment ([REQ-BIL-001.18](../billing-payments/requirement.md)). An organization member's cart becomes an order for organization-admin approval ([REQ-ORD-001](../order-lifecycle/requirement.md)), and payment follows approval.

## Actors
- Individual customer: owns a cart and pays for it.
- Organization member: owns a cart and submits it as an order for approval.
- Organization administrator (`MANAGE_ORDERS`): approves or rejects the order (existing REQ-ORD-001). Whether an admin's own cart skips approval is Open question 2.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-003.1 | **Add to cart:** **Buy** on a paid plan adds that product and plan to the signed-in user's cart and navigates to `/cart`. A product can be in the cart only once. Choosing Buy for another plan of the same product replaces the plan (to confirm in review). A toast confirms "Added {product} — {plan} to your cart". Free plans keep their current behaviour. | Must |
| REQ-MKT-003.2 | **Cart persistence:** the cart is stored on the server per user, so it survives sign-out and is the same on every device (to confirm in review). How long items stay in the cart is Not specified (Open question 3). | Must |
| REQ-MKT-003.3 | **View cart:** each item shows the product image, product name, plan, billing period, price and line amount. The cart shows the subtotal and one line per tax from [REQ-BIL-002](../tax-rules/requirement.md), using the region from the customer's billing details. If there are no billing details yet, it shows "Tax calculated at payment". It also shows the total and the item count. | Must |
| REQ-MKT-003.4 | **Edit cart:** the customer can remove an item, with **Undo** available for about 5 seconds. They can change the plan within the same product, or clear the whole cart after a confirmation. Quantity is shown only if a quantity model exists. Seats and quantity are not decided (D14), so there is no quantity stepper. | Must |
| REQ-MKT-003.5 | **Purchase validation:** every item is re-checked when the customer continues from the cart, and again when the checkout is created. A clear message is shown for each item in these cases: (a) the product is no longer published (not `ACTIVE`) or the plan no longer exists; (b) the price changed since the item was added — the old and new prices are shown and the customer must confirm the new price before continuing; (c) the customer already has an active subscription to that product; (d) the product requires another product the customer does not have (product dependencies, [REQ-CAT-001.6](../product-lifecycle/requirement.md)). | Must |
| REQ-MKT-003.6 | **Empty cart:** an icon, "Your cart is empty", "Find a product to get started." and a **Browse products** button. | Must |
| REQ-MKT-003.7 | **Cart badge:** the top bar shows a cart icon with the item count on every signed-in page. Clicking it opens `/cart`. | Must |
| REQ-MKT-003.8 | **Continue:** an individual's cart shows **Proceed to checkout**. This creates the subscriptions and their invoice, then opens the checkout ([REQ-BIL-001.18](../billing-payments/requirement.md)). An organization member's cart shows **Submit order for approval** with the helper text "Your organization admin approves orders before payment." This creates the order under [REQ-ORD-001](../order-lifecycle/requirement.md). After approval, the invoice is issued and paid through the same checkout. A successful continue empties the cart. | Must |
| REQ-MKT-003.9 | **Audit:** cart changes are shopping activity and are not written to the audit log. Submitting an order and paying are audited under their existing rules (REQ-ORD-001, REQ-BIL-001.17). To confirm in review. | Must |

## Out of scope
- Coupons, discounts, promotions ([C50](../../../01-business/roadmap/open-decisions.md#c50)).
- Product options and configuration (03.03.01.03): no model exists.
- Seats or quantity (D14, not decided). No quantity stepper.
- Partial payments, "Remaining amount" or "Custom amount", and instalments (shown in the reference image, but in no requirement). Excluded unless confirmed (Open question 4).
- Shipping, and any fees line (Open question 7).
- Anonymous carts. Buy asks a visitor to sign in first (Open question 5).
- Saving a cart for later, wish lists, or sharing a cart: Not specified.

## Dependencies
- Catalog: products, plans, prices, product media ([REQ-CAT-001](../product-lifecycle/requirement.md), [REQ-CAT-002](../plan-management/requirement.md)). Product media storage (D23) is not decided; products without an image show a generic icon.
- Tax: [REQ-BIL-002](../tax-rules/requirement.md).
- Subscriptions: today's subscribe flow (REQ-SUB-001). When a subscription becomes active relative to payment is [REQ-BIL-001](../billing-payments/requirement.md) Open question 3.
- Orders and approval: [REQ-ORD-001](../order-lifecycle/requirement.md). REQ-ORD-001 orders hold **one** product each, so how a cart with several items maps to orders is Open question 1.
- Checkout and payment: [REQ-BIL-001](../billing-payments/requirement.md) (.18–.21).
- New tables: [data model](../../../07-database/data-model/cart-checkout.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), business rules, workflow, acceptance criteria | this folder |
| Screens (UI) | [ui-requirements.md](ui-requirements.md) → [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) |
| API | [api-requirements.md](api-requirements.md) → [docs/06-api/api-requirements/cart-checkout.md](../../../06-api/api-requirements/cart-checkout.md) |
| Data model | [docs/07-database/data-model/cart-checkout.md](../../../07-database/data-model/cart-checkout.md) |
| Cross-feature flow | [docs/04-workflows/purchase-to-payment.md](../../../04-workflows/purchase-to-payment.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Several products in one cart:** can a cart hold plans from several products and be paid as **one invoice**? (Recommended: yes; to confirm.) Today an invoice bills one subscription, and a REQ-ORD-001 order holds one product. A "yes" means an invoice can cover several subscriptions, and an organization cart becomes either one order per item or a new multi-item order. Which one is Not specified. | Yes |
| 2 | Should an **organization admin's own cart** skip approval? | Yes |
| 3 | **Cart expiry:** how long does a cart keep its items? Not specified. | No — confirm in review |
| 4 | **Partial payments, custom amounts and instalments** (the reference shows "Remaining amount" and "Custom amount"): in no requirement. Excluded unless confirmed. | No — confirm in review |
| 5 | **Anonymous add to cart** before sign-in. Assumed no: Buy asks the visitor to sign in first. Whether the item is then added automatically after sign-in is Not specified. | No — confirm in review |
| 6 | **Replace plan on a second Buy** (.1), **server-side persistence** (.2) and **cart changes not audited** (.9): each "to confirm in review". | No — confirm in review |
| 7 | **Fees line** (for example payment-processing fees). Assumed none. | No — confirm in review |
| 8 | **Price-change confirmation** (.5 b): does the customer confirm per item, or once for the whole cart? Not specified. | No — confirm in review |
