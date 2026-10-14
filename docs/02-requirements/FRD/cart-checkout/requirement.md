# REQ-MKT-003 — Cart and checkout

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Every open question is answered ([C59](../../../01-business/roadmap/open-decisions.md#c59) defaults, [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-1 to CA-7, PB-1 and coupons); what remains is the product owner's confirmation of the engineering defaults (see [Open points](#open-points)).
**Built:** 2026-10-01 at the product owner's request, with the engineering defaults recorded under [C59](../../../01-business/roadmap/open-decisions.md#c59); **changed 2026-10-14 for [C100](../../../01-business/roadmap/open-decisions.md#c100)** (organization buyers and purchase requests with a reason, several products in one order and one invoice, seats × price, coupon field; migration [V040](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql)). Test cases: [TESTPLAN-MKT-003](../../../../test-cases/functional/cart-checkout/TESTPLAN-MKT-003.md).
**Decision:** [C59](../../../01-business/roadmap/open-decisions.md#c59) (cart, answer to D17), [C100](../../../01-business/roadmap/open-decisions.md#c100) (CA-1 to CA-7, PB-1 and the coupon field, 2026-10-14). Related: [C55](../../../01-business/roadmap/open-decisions.md#c55) (checkout, Pay by invoice), [C50](../../../01-business/roadmap/open-decisions.md#c50) (price books and promotions stay out; the coupon is the exception, [C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1), [C51](../../../01-business/roadmap/open-decisions.md#c51) (tax, REQ-BIL-002).

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
| 03.03.01.04 | Apply discount | **Coupon code only** ([C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1 note, REQ-MKT-003.18). Price books, plan discounts and other promotions stay excluded ([C50](../../../01-business/roadmap/open-decisions.md#c50)) |
| 03.03.01.05 | Accept terms | Yes, through the checkout consent checkbox ([REQ-BIL-001.18](../billing-payments/requirement.md)) |
| 03.03.01.06 | Submit order | Yes: Proceed to checkout, or Submit purchase request (.8, .11 to .13) |
| 03.03.02.01 | Validate eligibility | Yes: published product, existing active subscription (.5) |
| 03.03.02.02 | Validate payment | No. Payment verification is part of [REQ-BIL-001](../billing-payments/requirement.md) (.5–.8) |
| 03.03.02.03 | Validate dependencies | Yes: required products (.5), using the dependencies from [REQ-CAT-001.6](../product-lifecycle/requirement.md) |

## Summary
A signed-in customer collects paid plans in a server-side cart, reviews and edits them, and takes them to checkout. **Buy** on any paid plan adds the plan to the cart and opens `/cart`. The cart re-validates every item before payment. An individual's cart continues to billing details and payment ([REQ-BIL-001.18](../billing-payments/requirement.md)). An organization administrator's or Manage billing holder's cart is a **direct purchase** with one invoice; any other organization member's cart becomes **one purchase request with a reason** for an administrator to approve ([REQ-ORD-001](../order-lifecycle/requirement.md)), and payment follows approval. The customer can enter **one coupon code** in the cart ([C100](../../../01-business/roadmap/open-decisions.md#c100)).

## Actors
- Individual customer: owns a cart and pays for it.
- Organization member: owns a cart and submits it as **one purchase request with a reason** for approval.
- Organization administrator, or a member with **Manage billing** (`MANAGE_ORGANIZATION_BILLING`): buys directly, with no approval ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-2). When the buyer is not an administrator the organization administrators are notified.
- Organization administrator (`MANAGE_ORDERS`): approves or rejects purchase requests (existing REQ-ORD-001).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-003.1 | **Add to cart:** **Buy** on a paid plan adds that product and plan to the signed-in user's cart and navigates to `/cart`. A product can be in the cart only once. Choosing Buy for another plan of the same product replaces the plan (to confirm in review). A toast confirms "Added {product} — {plan} to your cart". Free plans keep their current behaviour. | Must |
| REQ-MKT-003.2 | **Cart persistence:** the cart is stored on the server per user, so it survives sign-out and is the same on every device (to confirm in review). **Items stay in the cart forever** — they never expire ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-3 C, answers Open question 3). | Must |
| REQ-MKT-003.3 | **View cart:** each item shows the product image, product name, plan, billing period, price and line amount. The cart shows the subtotal and one line per tax from [REQ-BIL-002](../tax-rules/requirement.md), using the region from the customer's billing details. If there are no billing details yet, it shows "Tax calculated at payment". It also shows the total and the item count. For an organization an item shows **seats × price** (REQ-MKT-003.17). | Must |
| REQ-MKT-003.4 | **Edit cart:** the customer can remove an item, with **Undo** available for about 5 seconds. They can change the plan within the same product, or clear the whole cart after a confirmation. There is **no quantity stepper** in the cart: seats are the organization's seats ([REQ-SUB-003](../subscription-seats/requirement.md)), shown on the item (REQ-MKT-003.17), and are changed afterwards on My subscriptions. | Must |
| REQ-MKT-003.5 | **Purchase validation:** every item is re-checked when the customer continues from the cart, and again when the checkout is created. A clear message is shown for each item in these cases: (a) the product is no longer published (not `ACTIVE`) or the plan no longer exists; (b) the price changed since the item was added — the old and new prices are shown and the customer must **confirm the new prices once for the whole cart** before continuing ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-6 A); (c) the customer already has an active subscription to that product; (d) the product requires another product the customer does not have (product dependencies, [REQ-CAT-001.6](../product-lifecycle/requirement.md)). | Must |
| REQ-MKT-003.6 | **Empty cart:** an icon, "Your cart is empty", "Find a product to get started." and a **Browse products** button. | Must |
| REQ-MKT-003.7 | **Cart badge:** the top bar shows a cart icon with the item count on every signed-in page. Clicking it opens `/cart`. | Must |
| REQ-MKT-003.8 | **Continue:** the cart's one primary button depends on who is buying (decided on the server, `continueAs`). An **individual** and an **organization administrator or Manage billing holder** see **Proceed to checkout**: this creates the subscriptions and **one invoice for the whole cart**, then opens the checkout ([REQ-BIL-001.18](../billing-payments/requirement.md)). Any **other organization member** sees **Submit purchase request** (REQ-MKT-003.12). A successful continue empties the cart. | Must |
| REQ-MKT-003.9 | **Audit:** cart changes are shopping activity and are not written to the audit log ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-7 A). Submitting a request, a direct purchase and paying are audited under their existing rules (REQ-ORD-001, REQ-BIL-001.17). | Must |
| REQ-MKT-003.10 | **Add to cart on the product card (C93):** **every** public product card shows **Add to cart** (Products catalog, platform pages, Home); for a product with no paid plan the button is shown **disabled** with the hint "No paid plan yet. Use Subscribe.". It adds the **cheapest paid plan** to the cart without leaving the page, shows "Added {product} — {plan} to your cart" with a **View cart** link, and updates the cart badge, so several products can be added in a row. The button is always "Add to cart" (no in-cart or subscribed state); a refused add shows the cart's message. A signed-out visitor signs in first and the product is then added. A product with only free plans or no plan is bought with **Subscribe**. | Must |

### Organization purchasing, one invoice and the coupon ([C100](../../../01-business/roadmap/open-decisions.md#c100))
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-003.11 | **One cart, one invoice** (CA-1 B). A cart with several products is paid with **one invoice** with one line per product; the same holds for an organization. All items must have one currency (`CURRENCY_MISMATCH` otherwise). | Must |
| REQ-MKT-003.12 | **Purchase request with a reason** (CA-1 B, CA-2). When an organization member who cannot buy directly continues, the cart becomes **one new multi-item order** (REQ-ORD-001) with the member's **reason**, which is required (up to 500 characters; a blank reason is refused and the cart is kept). The screen asks for the reason in a dialog before submitting. Every organization administrator is notified with the products and the reason. No invoice exists until an administrator approves; approval provisions every product and issues **one invoice**. The Complete step says the request was submitted. | Must |
| REQ-MKT-003.13 | **Direct purchase** (CA-2). An organization **administrator** or a member with **Manage billing** does not need approval: the cart creates an order recorded as approved by the buyer ("Purchased directly"), provisions every product and issues **one invoice**, then opens the checkout. When the buyer is not an administrator, **every organization administrator is notified** of the purchase ("A purchase was made for your organization"). Products purchased are turned ON for members by the default of [C90](../../../01-business/roadmap/open-decisions.md#c90) (AM-1). | Must |
| REQ-MKT-003.14 | **No instalments or custom amounts** (CA-4 A): a cart is paid in full, in one payment. | Must |
| REQ-MKT-003.15 | **Add after sign-in** (CA-5 A): a signed-out visitor who clicks Buy is sent to sign in and the chosen item is added to the cart after sign-in. | Must |
| REQ-MKT-003.16 | **One price confirmation for the whole cart** (CA-6 A): when one or more prices changed, the cart shows one notice with **Confirm new prices**; confirming accepts every changed price at once. Each item still shows its own "The price changed from … to …" message. | Must |
| REQ-MKT-003.17 | **Seats × price** (SE-1; engineering default to confirm). For an organization the plan price is per seat ([REQ-SUB-003.7](../subscription-seats/requirement.md)), so the cart shows each item as **{seats} seats × {price}** and its amount, and the totals are the amounts of the first invoice. An individual's item is one seat and shows no seats line. The seats are those the organization already has on the product, else its licensed seats. | Must |
| REQ-MKT-003.18 | **Coupon code** (PB-1 note, [C100](../../../01-business/roadmap/open-decisions.md#c100)). The cart summary has a **Coupon code** field with **Apply**. **One code per cart**, no stacking. The discount applies to the cart subtotal, is shown as a line "Discount ({code})" with a minus amount, the total is net of it, and tax is calculated on the discounted amounts at invoicing ([REQ-BIL-001.35](../billing-payments/requirement.md)). A code that is not valid, not active, expired, in another currency, or worth the whole cart or more, is refused with the reason and the field stays; **Remove coupon** clears it. If an applied code stops being usable (disabled or expired) the cart keeps showing it with the reason and no discount, and checkout is **refused** with that reason, creating nothing, until the customer removes the code or another is applied. **A purchase request carries no coupon:** the field is hidden for a member who submits a request, and the server refuses it. | Must |
| REQ-MKT-003.19 | **Defaults confirmed** ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-7 A): a second Buy of the same product replaces the plan (REQ-MKT-003.1); the cart is saved on the server (.2); cart changes are not audited (.9); there is **no fees line**. | Must |

## Out of scope
- Price books, plan-level discounts, usage billing and any promotion other than the coupon code ([C50](../../../01-business/roadmap/open-decisions.md#c50), [C100](../../../01-business/roadmap/open-decisions.md#c100) PB-1 A). Coupon limits (maximum redemptions, per customer, minimum amount, first purchase only), scope to products or plans, and stacking are **Not specified**.
- Product options and configuration (03.03.01.03): no model exists.
- A quantity stepper in the cart. Seats are changed on My subscriptions ([REQ-SUB-003](../subscription-seats/requirement.md)).
- Partial payments, "Remaining amount" or "Custom amount", and instalments ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-4 A).
- Shipping, and any fees line ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-7 A).
- Anonymous carts. Buy asks a visitor to sign in first and adds the item after sign-in (CA-5 A).
- A coupon on a purchase request from a member (it is applied only when someone who pays buys).
- Saving a cart for later, wish lists, or sharing a cart: Not specified.

## Dependencies
- Catalog: products, plans, prices, product media ([REQ-CAT-001](../product-lifecycle/requirement.md), [REQ-CAT-002](../plan-management/requirement.md)). Product media storage (D23) is not decided; products without an image show a generic icon.
- Tax: [REQ-BIL-002](../tax-rules/requirement.md).
- Subscriptions: today's subscribe flow (REQ-SUB-001). When a subscription becomes active relative to payment is [REQ-BIL-001](../billing-payments/requirement.md) Open question 3.
- Orders and approval: [REQ-ORD-001](../order-lifecycle/requirement.md). An order now holds several products and a reason ([REQ-ORD-001.8 to .11](../order-lifecycle/requirement.md)); a cart becomes one order.
- Seats and per-seat pricing: [REQ-SUB-003.7](../subscription-seats/requirement.md). Coupons and invoice discounts: [REQ-BIL-001.34 to .36](../billing-payments/requirement.md). Notifications to organization administrators.
- Checkout and payment: [REQ-BIL-001](../billing-payments/requirement.md) (.18–.21).
- New tables and columns: [data model](../../../07-database/data-model/cart-checkout.md) (V015; V040 adds `cart.coupon_code`, `orders.request_reason` and `order_item`).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), business rules, workflow, acceptance criteria | this folder |
| Screens (UI) | [ui-requirements.md](ui-requirements.md) → [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) |
| API | [api-requirements.md](api-requirements.md) → [docs/06-api/api-requirements/cart-checkout.md](../../../06-api/api-requirements/cart-checkout.md) |
| Data model | [docs/07-database/data-model/cart-checkout.md](../../../07-database/data-model/cart-checkout.md) |
| Cross-feature flow | [docs/04-workflows/purchase-to-payment.md](../../../04-workflows/purchase-to-payment.md) |

## Open points
Raised for the product owner ([C100](../../../01-business/roadmap/open-decisions.md#c100)); none blocks use of the feature.
1. **The cart shows seats × price** for an organization, which is a consequence of per-seat pricing, an **engineering default to confirm** ([REQ-SUB-003.7](../subscription-seats/requirement.md#open-points)). The seats come from the organization's existing seats on the product, else its licensed seats; the buyer cannot choose a different number in the cart. If the product owner wants the buyer to choose seats at purchase, that is new scope.
2. **No organization "Renew now" endpoint** (see [REQ-SUB-004 open points](../renewal-reminders/requirement.md#open-points)); not a cart matter, listed because renewal and purchase meet on My subscriptions.
3. **A coupon survives Clear cart on the server.** Clearing the cart, or removing its last item, does not remove the applied coupon code from the cart row; it comes back when items are added again ([BR-20](business-rules.md)). The intended rule is that an empty cart has no coupon; fix or accept.
4. **Complete step wording.** After a request the Complete step still reads "Order submitted for approval … approves orders before payment"; only the cart's own text says "purchase request". Align the wording if wanted.

## Open questions
All answered. None remains open.

| # | Question | Answer |
|---|---|---|
| 1 | ~~**Several products in one cart:** paid as **one invoice**? How does an organization cart become orders?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-1 B.** One invoice for the cart; an organization cart becomes **one new multi-item order** ([REQ-MKT-003.11, .12](#functional-requirements)). |
| 2 | ~~Should an **organization admin's own cart** skip approval?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-2.** Yes; so does a person with Manage billing, and the administrators are notified when the buyer is not an administrator. Other members raise a request with a reason ([REQ-MKT-003.12, .13](#functional-requirements)). |
| 3 | ~~**Cart expiry:** how long does a cart keep its items?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-3 C.** Forever ([REQ-MKT-003.2](#functional-requirements)). |
| 4 | ~~**Partial payments, custom amounts and instalments.**~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-4 A.** None ([REQ-MKT-003.14](#functional-requirements)). |
| 5 | ~~**Anonymous add to cart** before sign-in.~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-5 A.** The chosen item is added after sign-in ([REQ-MKT-003.15](#functional-requirements)). |
| 6 | ~~**Replace plan on a second Buy**, **server-side persistence** and **cart changes not audited**.~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-7 A.** All three stand ([REQ-MKT-003.19](#functional-requirements)). |
| 7 | ~~**Fees line.**~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-7 A.** None. |
| 8 | ~~**Price-change confirmation:** per item, or once for the whole cart?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-6 A.** Once for the whole cart ([REQ-MKT-003.16](#functional-requirements)). |
