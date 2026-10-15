# Screen: Cart

| Field | Value |
|---|---|
| Requirement | [REQ-MKT-003.1–.9, .11–.19](../../02-requirements/FRD/cart-checkout/requirement.md) |
| Decision | [C59](../../01-business/roadmap/open-decisions.md#c59), [C100](../../01-business/roadmap/open-decisions.md#c100) (CA-1 to CA-7, PB-1 coupon) |
| Built | 2026-10-01 — `frontend/src/pages/CartPage.tsx` (defaults under C59); changed 2026-10-14 for C100 (coupon field, seats × price, one price confirmation, purchase-request dialog, organization buyer) |
| Route | `/cart`, inside the signed-in tool frame ([application-layout.md](application-layout.md)) |
| Entry points | **Buy** / **Subscribe** on any paid plan (catalog, product detail, plan comparison); the cart icon in the top bar; **Edit cart** and the breadcrumb's **Cart** step on the [checkout](checkout-payment.md) |
| Permissions | Any signed-in user, for their own cart only. Visitors who are not signed in are asked to sign in first (REQ-MKT-003 Open question 5) |
| Next step | Individual, organization administrator or Manage billing holder: **Proceed to checkout** → [checkout-payment.md](checkout-payment.md). Other organization member: **Submit purchase request** → one order under [REQ-ORD-001](../../02-requirements/FRD/order-lifecycle/requirement.md), after the [reason dialog](#purchase-request-dialog) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Colours from the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45)), light and dark mode. Logos: [payment-brand-assets.md](payment-brand-assets.md).

> **Free trial ([C102](../../01-business/roadmap/open-decisions.md#c102)).** A customer buys over a free trial through this cart: the purchase turns the trial subscription into a normal one. Known gap: while the trial is still running the cart reports the item as already subscribed ([REQ-MKT-003.20](../../02-requirements/FRD/cart-checkout/requirement.md)); it works once the trial has ended.

## Layout

### Desktop (≥ 1024 px)
Two columns.

**Left (about 65 %)**
- Heading **"Your cart"** with the item count, for example "Your cart (2)" (`cart.title`).
- One **item card** per item, stacked:
  - product image on the left, 64 × 64, rounded, from the catalog's product media; a generic product icon if there is none (D23, product media storage, is not decided);
  - **product name** (bold) and plan name;
  - billing-period chip — **Monthly** / **Yearly** — and the term the subscription will cover (for example "1 Oct 2026 – 31 Oct 2026");
  - **Change plan**: a select with the same product's published paid plans;
  - for an **organization** with more than one seat, the line **"{seats} seats × {price}"** under the plan name (`cart.seatsTimesPrice`); no such line for one seat;
  - price on the right, large: the **line amount** (price × seats for an organization);
  - **Remove** (trash icon + text) at the bottom-right.
  - Any validation issue for the item appears as an **inline alert inside its card** (see [Validation messages](#validation-messages)).
- Below the list: **Continue browsing** (text link, left, to the catalog) and **Clear cart** (text button, right).

**Right (about 35 %), sticky while scrolling**
- **Order summary** card:
  - item count;
  - **Subtotal**;
  - when a coupon is applied and gives a discount, **"Discount ({code})"** with a minus amount (`cart.coupon.discount`);
  - one line per tax with name and rate, for example "GST (18 %)", from [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) using the region in the customer's billing details — or **"Tax calculated at payment"** if there are no billing details yet;
  - divider; **Total** (large, bold);
  - the **coupon** area, below the total and above the button, shown to everyone **except** an organization member whose cart is a purchase request (see [Coupon](#coupon));
  - primary full-width button: **Proceed to checkout** (individual, organization administrator, Manage billing holder) or **Submit purchase request** (other organization member), with the helper text **"You cannot buy products yourself. Your organization admin reviews the request and can purchase them for you."** (`cart.approvalHelp`) for a member, and **"If you are not an organization admin, the organization admins are notified of this purchase."** (`cart.buyerNote`) for an organization buyer;
  - trust line: lock icon, **"Secure payments by Razorpay"**, and a small row of the accepted payment logos ([payment-brand-assets.md](payment-brand-assets.md)). *As built this line is fixed text (`cart.summary.trust`) and still names Razorpay, even when another gateway is the default ([C98](../../01-business/roadmap/open-decisions.md#c98)); making it follow the routed gateway is a follow-up.*
- No shipping, no fees line ([C100](../../01-business/roadmap/open-decisions.md#c100) CA-7 A), no quantity stepper (seats are changed on My subscriptions). Price books and other promotions stay out ([C50](../../01-business/roadmap/open-decisions.md#c50)); the coupon code is the exception ([C100](../../01-business/roadmap/open-decisions.md#c100) PB-1 note).

### Tablet (600–1023 px)
Same two columns, narrower; the summary moves below the items when it does not fit. Not otherwise specified.

### Mobile (< 600 px)
- One column of item cards.
- The summary becomes a **sticky bottom bar** with the total and the primary button. **View summary** expands the full summary card.

## Behaviour
| Action | Behaviour |
|---|---|
| Remove | The item disappears and the totals update. A toast **"Removed {product} · Undo"** shows for about 5 seconds; **Undo** restores the item with the same plan. |
| Change plan | The line, subtotal, tax and total update immediately (optimistic). If the server refuses, the previous plan and totals are restored and an error toast is shown. |
| Clear cart | Confirmation dialog **"Remove all items from your cart?"** with **Clear cart** / **Cancel**. |
| Proceed / Submit | For a member's purchase request the primary button first opens the [reason dialog](#purchase-request-dialog). Then it runs validation (`POST /me/cart/validate`). If any item has an issue, the page scrolls to the first one, moves focus to its alert, and the customer stays on the cart (the dialog closes). Otherwise it calls `POST /me/cart/checkout`: individual, administrator or Manage billing holder → opens `/checkout?invoiceId=…&from=cart` (one invoice for the whole cart); member → `/checkout?orders={id}&step=complete`, the Complete view "Order submitted for approval" ([checkout-payment.md](checkout-payment.md#step-complete)). If the server refuses (for example a coupon that stopped working) the message is shown in a toast, the cart is kept and reloaded. The button shows a spinner and is disabled while the request runs. |
| Confirm new prices | One **warning notice above the items** — "The price of 1 item in your cart changed. Confirm the new price once for the whole cart to continue." / "The prices of {n} items … Confirm the new prices once for the whole cart to continue." (`cart.pricesChanged`) — with one **Confirm new prices** button ([C100](../../01-business/roadmap/open-decisions.md#c100) CA-6 A). It accepts every changed price (one `PATCH …/items/{id}` with `confirmPrice` per changed item) and clears the item alerts. Each item still shows its own "The price changed from {old} to {new}." message without a button of its own. |
| Buy anywhere | Every Buy / Subscribe on a paid plan adds to the cart and navigates here, with the toast **"Added {product} — {plan} to your cart"**. Free plans keep their current behaviour. |

## Validation messages
Shown inline in the item's card, in the theme's warning (price) or error style.

| Issue (BR-6) | Message | Action in the alert | i18n key |
|---|---|---|---|
| `NOT_AVAILABLE` (product) | "This product is no longer available." | **Remove** | `cart.issue.productUnavailable` |
| `NOT_AVAILABLE` (plan) | "This plan is no longer available. Choose another plan." | **Change plan** focused | `cart.issue.planUnavailable` |
| `PRICE_CHANGED` | "The price changed from {old} to {new}." | none in the item; **Confirm new prices** is the notice above the items (one for the whole cart) | `cart.issue.priceChanged`, `cart.confirmPrice` |
| `ALREADY_SUBSCRIBED` | "You already have an active subscription to {product}." | **Remove** | `cart.issue.alreadySubscribed` |
| `MISSING_DEPENDENCY` | "{product} requires {required}." | **Add {required}** (opens that product) and **Remove** | `cart.issue.missingDependency` |
| `CURRENCY_MISMATCH` | "This item is priced in a different currency from the rest of your cart. Check out separately." | **Remove** | `cart.issue.currencyMismatch` |
| `NOT_ELIGIBLE` | "{product} is not available for your account type." | **Remove** | `cart.issue.notEligible` |

## Coupon
[C100](../../01-business/roadmap/open-decisions.md#c100) PB-1 note, REQ-MKT-003.18. In the order summary, below the total.
| State | Content |
|---|---|
| No coupon | A text field **"Coupon code"** (up to 40 characters, typed letters shown in capitals) and an **Apply** button (disabled while empty or busy). Enter submits. |
| Applied | A green outlined chip **"Coupon {code}"** and a **Remove coupon** button replace the field. The summary shows **Discount ({code})** with a minus amount and the total net of it. |
| Refused | The server's reason is shown as helper text under the field (for example "This coupon has expired.", "This coupon is for USD purchases only.", "This coupon is worth more than your cart. Add more to your cart to use it."); the typed text stays. A refusal without a message shows "This coupon code could not be applied." (`cart.coupon.error`) |
| Applied but no longer usable | The chip and **Remove coupon** stay, the discount line is gone, and a warning alert shows the reason (`couponMessage`). **Proceed** is then refused by the server with that reason until the coupon is removed |
| Purchase request (organization member) | **No coupon area at all.** A coupon cannot be used on a request |
Tax is not calculated in the cart (it is calculated on the invoice, on the discounted amounts).

## Purchase request dialog
[C100](../../01-business/roadmap/open-decisions.md#c100) CA-1 B, CA-2, REQ-MKT-003.12. Opened by **Submit purchase request** (an organization member who cannot buy directly), before validation.
- Dialog title **"Submit a purchase request"** (`aria-labelledby`); text "Your organization admin reviews this request and can purchase the products for you. Tell them why you need them."
- A multi-line field **"Why do you need this?"**, required, at most 500 characters, with the counter "{n} / 500 characters" and autofocus. Submitting it empty shows "Tell your organization admin why you need this." and sends nothing.
- Buttons **Cancel** and **Submit request** (spinner while the request runs; both disabled then).
- On success the dialog closes and the customer goes to the Complete step. If validation finds issues the dialog closes and the issues are shown on the cart.

## States
| State | Behaviour |
|---|---|
| Loading | Skeletons matching the item cards and the summary |
| Empty | Centred cart icon, **"Your cart is empty"**, **"Find a product to get started."**, button **Browse products** (to the catalog) |
| Error | Backend message as-is with **Retry** |
| Gateway not configured | The cart works normally; the checkout shows the gateway banner ([checkout-payment.md](checkout-payment.md#gateway-not-configured)) |

## Accessibility
- Heading structure: page `h1` "Your cart", each item name an `h2`/`h3`.
- **Remove**, **Undo**, **Change plan**, **Clear cart** and **Confirm new price** are keyboard operable with visible focus; Remove's accessible name includes the product ("Remove Valam.ai").
- The Undo toast stays at least 5 seconds and pauses while focused or hovered; it is announced with `aria-live="polite"`.
- Validation alerts use `role="alert"` when they appear after Proceed; focus moves to the first one.
- Every logo has alt text with the brand name; product images have the product name as alt text (empty alt when the name is visible next to it).
- The coupon field has a visible label and its error is helper text; the warning for an unusable coupon is an alert; the purchase-request dialog is labelled and its field is required with a visible counter.
- axe tests for the cart, the Clear cart dialog, the toasts, the coupon refusal and warning, and the purchase-request dialog (REQ-MKT-003 AC-23, AC-37).

## i18n
All strings in `en.json` and `es.json` under `cart.*`. Amounts and dates use the platform formatting.

## API used
[cart-checkout.md](../../06-api/api-requirements/cart-checkout.md): `GET /me/cart`, `POST /me/cart/items`, `PATCH /me/cart/items/{itemId}`, `DELETE /me/cart/items/{itemId}`, `DELETE /me/cart`, `POST /me/cart/validate`, `POST /me/cart/checkout` (body `{ reason }` for a purchase request), `PUT /me/cart/coupon`, `DELETE /me/cart/coupon`.

## Add to cart on the product card (C93, REQ-MKT-003.10)
Every public product card shows an outlined **Add to cart** button (cart icon; accessible name "Add {product} to cart") beside **Subscribe**, on the Products catalog, platform pages and Home. A click adds the cheapest paid plan and shows a bottom notice "Added {product} — {plan} to your cart" with **View cart**; the cart badge updates and the page does not move. The button is disabled while the add runs. A refusal shows the cart's message in the same notice. A product with no paid plan shows the button disabled with the hint "No paid plan yet. Use Subscribe." (`cart.noPaidPlan`). Admin grids have no such button. Strings under `cart.addToCart`, `cart.addToCartLabel`, `cart.viewCart`. Built in `frontend/src/components/cart/useAddToCart.tsx` and `ProductTile`.
