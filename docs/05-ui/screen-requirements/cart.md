# Screen: Cart

| Field | Value |
|---|---|
| Requirement | [REQ-MKT-003.1–.9](../../02-requirements/FRD/cart-checkout/requirement.md) |
| Decision | [C59](../../01-business/roadmap/open-decisions.md#c59) |
| Route | `/cart`, inside the signed-in tool frame ([application-layout.md](application-layout.md)) |
| Entry points | **Buy** / **Subscribe** on any paid plan (catalog, product detail, plan comparison); the cart icon in the top bar; **Edit cart** and the breadcrumb's **Cart** step on the [checkout](checkout-payment.md) |
| Permissions | Any signed-in user, for their own cart only. Visitors who are not signed in are asked to sign in first (REQ-MKT-003 Open question 5) |
| Next step | Individual: **Proceed to checkout** → [checkout-payment.md](checkout-payment.md). Organization member: **Submit order for approval** → order under [REQ-ORD-001](../../02-requirements/FRD/order-lifecycle/requirement.md) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Colours from the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45)), light and dark mode. Logos: [payment-brand-assets.md](payment-brand-assets.md).

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
  - price on the right, large;
  - **Remove** (trash icon + text) at the bottom-right.
  - Any validation issue for the item appears as an **inline alert inside its card** (see [Validation messages](#validation-messages)).
- Below the list: **Continue browsing** (text link, left, to the catalog) and **Clear cart** (text button, right).

**Right (about 35 %), sticky while scrolling**
- **Order summary** card:
  - item count;
  - **Subtotal**;
  - one line per tax with name and rate, for example "GST (18 %)", from [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) using the region in the customer's billing details — or **"Tax calculated at payment"** if there are no billing details yet;
  - divider; **Total** (large, bold);
  - primary full-width button: **Proceed to checkout** (individual) or **Submit order for approval** (organization member) with the helper text **"Your organization admin approves orders before payment."**;
  - trust line: lock icon, **"Secure payments by Razorpay"**, and a small row of the accepted payment logos ([payment-brand-assets.md](payment-brand-assets.md)).
- No coupon field ([C50](../../01-business/roadmap/open-decisions.md#c50)), no shipping, no fees line (REQ-MKT-003 Open question 7), no quantity stepper (D14 not decided).

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
| Proceed / Submit | Runs validation first (`POST /me/cart/validate`). If any item has an issue, the page scrolls to the first one, moves focus to its alert, and the customer stays on the cart. Otherwise it calls `POST /me/cart/checkout`: individual → opens `/checkout?invoiceId=…`; organization member → the Complete view "Order submitted for approval" ([checkout-payment.md](checkout-payment.md#step-complete)). The button shows a spinner and is disabled while the request runs. |
| Confirm new price | Shown inside a **price changed** alert. Accepts the new price for that item (`PATCH …/items/{id}` with `confirmPrice`) and clears the alert. Per item or for the whole cart: REQ-MKT-003 Open question 8 (per item proposed). |
| Buy anywhere | Every Buy / Subscribe on a paid plan adds to the cart and navigates here, with the toast **"Added {product} — {plan} to your cart"**. Free plans keep their current behaviour. |

## Validation messages
Shown inline in the item's card, in the theme's warning (price) or error style.

| Issue (BR-6) | Message | Action in the alert | i18n key |
|---|---|---|---|
| `NOT_AVAILABLE` (product) | "This product is no longer available." | **Remove** | `cart.issue.productUnavailable` |
| `NOT_AVAILABLE` (plan) | "This plan is no longer available. Choose another plan." | **Change plan** focused | `cart.issue.planUnavailable` |
| `PRICE_CHANGED` | "The price changed from {old} to {new}." | **Confirm new price** | `cart.issue.priceChanged` |
| `ALREADY_SUBSCRIBED` | "You already have an active subscription to {product}." | **Remove** | `cart.issue.alreadySubscribed` |
| `MISSING_DEPENDENCY` | "{product} requires {required}." | **Add {required}** (opens that product) and **Remove** | `cart.issue.missingDependency` |

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
- axe tests for the cart, the Clear cart dialog and the toasts (REQ-MKT-003 AC-23).

## i18n
All strings in `en.json` and `es.json` under `cart.*`. Amounts and dates use the platform formatting.

## API used
[cart-checkout.md](../../06-api/api-requirements/cart-checkout.md): `GET /me/cart`, `POST /me/cart/items`, `PATCH /me/cart/items/{itemId}`, `DELETE /me/cart/items/{itemId}`, `DELETE /me/cart`, `POST /me/cart/validate`, `POST /me/cart/checkout`.
