# Screen: Checkout payment

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.18, .19, .22, .23](../../02-requirements/FRD/billing-payments/requirement.md); entry from the cart: [REQ-MKT-003.8](../../02-requirements/FRD/cart-checkout/requirement.md) |
| Decisions | [C55](../../01-business/roadmap/open-decisions.md#c55) (online and offline payment, card-panel approach); [C59](../../01-business/roadmap/open-decisions.md#c59) (cart; this layout) |
| Route | `/checkout?invoiceId=…` (also `?subscriptionId=…`, and `&scope=organization` for an organization's invoice), inside the signed-in tool frame ([application-layout.md](application-layout.md)) |
| Entry points | **Proceed to checkout** on the [cart](cart.md); **Pay** on an OPEN invoice (Billing → Invoices); the invoice issued after an organization order is approved |
| Permissions | The customer who owns the invoice, or an organization billing user (REQ-BIL-001 Open question 5). Anyone else: 404 |
| Card data | [BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md) — see [Card data approach](#card-data-approach-br-bil-001) |
| Logos | [payment-brand-assets.md](payment-brand-assets.md) |

Shared presentation rules (theme, status chips, money formatting, loading/empty/error, toasts, accessibility, i18n, gateway banner): [billing-ui-standards.md](billing-ui-standards.md). Colours come only from the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45), `frontend/src/theme.ts`), in light and dark mode. One screen with steps ([C44](../../01-business/roadmap/open-decisions.md#c44)).

> **Layout history.** The first C55 layout (stepper on top, vertical option cards, tinted "Your order" panel) was built on 2026-10-01 (`frontend/src/pages/CheckoutPage.tsx`). This file now describes the **C59 redesign**, after the two reference screenshots supplied with the request (not stored in the repository). It is Draft until REQ-BIL-001 (.18, .22, .23) and REQ-MKT-003 are approved; the built page changes in Phase 2. Elements of the reference that no requirement covers are **not** part of this screen: shipping, shipping courier, fees, "Remaining amount", "Custom amount" and instalments (REQ-MKT-003 Open question 4), and any third-party merchant branding.

## Page frame

### Desktop (≥ 1024 px)
- **Background:** a soft branded backdrop in the theme's primary colour with subtle decorative curves or waves (as in the reference, in EIS brand colours). Decorative only: `aria-hidden`, no text, no information. In dark mode it uses the dark-palette primary at low opacity.
- **A centred card** (`background.paper`, rounded, elevated) holding two columns:
  - **Left — steps** (about 62 %).
  - **Right — cart summary** (about 38 %), on a light-grey surface (`background.default` / `action.hover`, so it adapts to dark mode).
- **Header (left column):** the EIS logo and name (in place of the reference's "MERCHANT NAME"), and below it a **breadcrumb stepper**: **Cart › Billing details › Payment › Complete**. The current step is bold; earlier steps are links back to them; later steps are plain text. When the checkout did not start from the cart (paying an invoice from Billing), the **Cart** crumb is hidden (proposed — REQ-BIL-001 Open question 22).
- **Below the card:** "Powered by Razorpay" badge on the left ([payment-brand-assets.md](payment-brand-assets.md)); links **Refund policy · Privacy policy · Terms of service** on the right (URLs Not specified — REQ-BIL-001 Open questions 12 and 20; until known the links are not rendered).

### Tablet (600–1023 px)
Same card; the right column narrows. If the content does not fit, the summary moves above the steps as on mobile. Not otherwise specified.

### Mobile (< 600 px)
- One column; the decorative backdrop is reduced to a thin band.
- The cart summary collapses into a **"Total {total} · {n} items"** bar at the top that expands to the full summary.
- Payment tiles wrap into a **2-column grid**.
- The **Pay** / **Generate invoice** button is sticky at the bottom of the viewport.

## Step: Cart
The [cart page](cart.md) (`/cart`). The breadcrumb's **Cart** link returns there. On the checkout itself, items are read-only.

## Step: Billing details
Pre-filled from the customer's billing details (REQ-BIL-001.1). Same fields and validation as before:

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Billing name | Text | Yes | Non-blank, max 200 | `checkout.details.name` |
| Billing email | Email | Yes | Valid email | `checkout.details.email` |
| Address line 1 | Text | Yes | Non-blank, max 200 | `checkout.details.address1` |
| Address line 2 | Text | No | Max 200 | `checkout.details.address2` |
| City | Text | Yes | Non-blank | `checkout.details.city` |
| State / region | Text | Yes | Non-blank | `checkout.details.state` |
| Postal code | Text | Yes | Non-blank; format by country Not specified | `checkout.details.postalCode` |
| Country | Select | Yes | From a country list | `checkout.details.country` |
| Tax ID | Text | No | Format Not specified ([REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) Open questions 4–5) | `checkout.details.taxId` |

Buttons: **Continue to payment** (primary; saves the details). If the details are already complete, the checkout opens directly on **Payment**.

## Step: Payment

### Billing-details summary (top of the left column)
A bordered box with two rows, each: label (muted), value, and a **Change** link on the right that reopens the Billing details step.

| Row | Value | i18n key |
|---|---|---|
| Contact | Billing email | `checkout.review.contact` |
| Billing address | Address lines, city, state, postal code, country; the tax ID if present | `checkout.review.address` |

### Payment header
- Heading **"Payment"** (`checkout.payment.title`) and subtitle **"All transactions are secure and encrypted."** (`checkout.payment.subtitle`).
- On the right: **Amount due** with a circular, progress-style badge containing the total in the platform currency (`checkout.payment.amountDue`). It is **display only** and always full: there are no partial payments (REQ-MKT-003 Open question 4).

### Payment method tiles
A horizontal row of equal-size tiles, each with an icon or logo and a label. The **selected** tile is filled with the primary colour with white (contrast-text) label and icon; the others are outlined. The panel for the selected tile opens below the row. Tiles are a radio group (see Accessibility).

| Tile | i18n key | Tile visual | Panel below the tiles |
|---|---|---|---|
| **Card** | `checkout.option.card` | Visa, Mastercard, RuPay and American Express logos | **Saved cards** (REQ-BIL-001.22): one row each — radio, network logo, `•••• •••• •••• 2860`, name on card (if Razorpay returns it), expiry `MM/YY`; an **Expired** chip on a card past its expiry (that row cannot be selected); **Remove** in the row's overflow menu (confirmation, then the same delete as Billing → Payment methods). Then **+ Use another card**, which reveals the card fields — see [Card data approach](#card-data-approach-br-bil-001) — and the checkbox **"Save this card securely with Razorpay for future payments"** (unticked by default; consent per BR-BIL-001). With no saved cards, the card fields are shown directly. |
| **UPI** | `checkout.option.upi` | UPI logo, with Google Pay, PhonePe, Paytm and BHIM logos | Saved UPI IDs, if Razorpay returns any (REQ-BIL-001 Open question 18). Then **Pay with UPI ID** or **Pay with a UPI app** (QR on desktop, app intent on mobile) — both happen in Razorpay's window unless a UPI ID typed on the EIS page can be passed to Razorpay Standard Checkout (Open question 18). |
| **Netbanking** | `checkout.option.netbanking` | Bank-building icon (MUI icon, no bank logos) | "You'll choose your bank in Razorpay's secure window." Which banks: provider-defined. |
| **Wallets** | `checkout.option.wallets` | Wallet icon (MUI icon). No wallet brand logos until the wallet list is confirmed (REQ-BIL-001 Open question 19) | "You'll choose your wallet in Razorpay's secure window." |
| **Pay by invoice** | `checkout.option.offline` | Invoice/document icon | C55 content: "We'll issue an invoice now. Pay by bank transfer, NEFT/RTGS or cheque, quoting the invoice number." The billing email the invoice will be sent to. Payment terms: Not specified until decided (REQ-BIL-001 Open question 10). **Shown only to customers allowed to use it** (Open question 9; built default: everyone). |

**Netbanking** and **Wallets** replace the single C55 "Other online methods" option (REQ-BIL-001 Open question 17).

### Card data approach (BR-BIL-001)
Mandatory rule: no EIS frontend state, API, log, database column or analytics event may ever hold a full card number, CVV or expiry entered by the customer.

- **Preferred:** provider-hosted secure card fields (iframes the EIS page cannot read), laid out as in the reference — **Card number** (full width, with the live network logo on the right), then **Name on card**, **Expiry (MM/YY)** and **CVV** (with a help tooltip). EIS code never reads their values.
- **Approach used: placeholder fields + Razorpay Checkout** ([C55](../../01-business/roadmap/open-decisions.md#c55)). No Razorpay product offering embeddable secure card fields could be confirmed: Razorpay's Custom UI integration has the merchant page collect the card values itself, which BR-BIL-001 forbids. razorpay.com was again not reachable from the build environment on 2026-10-01 (network egress blocked), and a web search found no official Razorpay documentation for hosted card fields. Therefore:
  - The card fields under **+ Use another card** keep the reference layout but are **read-only placeholders** (not inputs, not in the tab order, decorative values `aria-hidden`), with the note "You'll enter your card details in Razorpay's secure window."
  - **Pay** opens Razorpay Standard Checkout with the **card** method preselected (for a saved card: that saved card). The customer types or confirms the card there; card validation messages appear inside Razorpay's window.
  - Only the "Save this card" consent (true/false) is sent to EIS.
- If Razorpay is later confirmed to offer embeddable secure fields (REQ-BIL-001 Open question 15), they replace the placeholders in the same layout.

### Terms and Pay
- Above the button, a required checkbox, unticked: **"I agree to the Terms & Conditions, Refund policy and Privacy policy"** (`checkout.consent`). The three are links once their URLs exist (Not specified — Open questions 12 and 20); until then, plain text.
- At the bottom-right of the payment box: **"Pay | {total}"** (`checkout.pay`, for example "Pay | ₹105.00") — label and total separated by a thin divider; **"Generate invoice"** (`checkout.generateInvoice`) when Pay by invoice is selected.
- Disabled until a method is ready (a tile selected; for Card, a saved card selected or **Use another card** open) **and** the terms checkbox is ticked. While a request is in progress the button is disabled and shows a spinner (double-click protection).

### Gateway not configured
Card, UPI, Netbanking and Wallets tiles appear **disabled** with a tooltip "Online payments are not available yet." and the shared banner (`billing.gateway.notConfigured`, [billing-ui-standards.md](billing-ui-standards.md#gateway-banner)). Pay by invoice stays available if allowed. If no tile is enabled, the Pay button stays disabled.

### Footer bar (bottom of the left column)
- Left: **‹ Step back** — to the previous step (Billing details; from Billing details, to the cart).
- Right: **Complete order**, disabled until the payment result is confirmed (online payment captured, or the offline invoice generated). Then it moves to the **Complete** step. It mirrors the reference's two-level action and **never starts a payment**: Pay (or Generate invoice) is the only action that does. If the result is confirmed automatically, the page moves to Complete without waiting for the click.

## Cart summary (right column)
- **Subtotal**; **one line per tax** with its name and rate, for example **"GST (18 %)"** ([REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md)), or **"Tax: none for this region"**; **Total** in large type in the primary colour.
- **No shipping row** (subscription software) and **no fees row** unless a fee rule exists (Not specified — REQ-BIL-001 Open question 21). No coupon field ([C50](../../01-business/roadmap/open-decisions.md#c50)).
- **Items:** product image (catalog media; generic product icon if none) with a small count badge, product name, plan, price. Quantity is not shown (D14 not decided).
- **Edit cart** link to `/cart` (only when the checkout started from the cart). Items are read-only here; Remove happens on the cart page.
- The summary stays visible on every step.

## Step: Complete
Focus moves to the result heading. Status changes are announced with `aria-live="polite"`.

| Result | Content | Actions | i18n key |
|---|---|---|---|
| **Online payment successful** | Success check, "Payment successful", invoice number, amount paid, method (for example "Visa •••• 4242" or "UPI" — display fields from Razorpay only), date | **Download receipt**, **Download invoice**, **Go to my products**, **View billing** | `checkout.result.success` |
| **Online payment pending** | Spinner, "Confirming your payment…". Polls every **5 seconds** for up to **2 minutes** (C55 default), then "This is taking longer than usual…" with **View billing** | — | `checkout.result.pending` |
| **Online payment failed / cancelled** | Error icon, the provider's reason, "Your invoice is still open." | **Try again** (back to Payment with the same tile), **Choose another method** | `checkout.result.failed` |
| **Invoice generated (offline)** | Document icon, "Invoice generated", invoice number, total, due date (Open question 10), **bank details** (account name, bank, account number, IFSC, SWIFT/BIC — [admin-billing-settings.md](admin-billing-settings.md)) each with a **Copy** button; "Quote invoice number {number} as the payment reference"; "We've emailed the invoice to {billing email}"; subscription status | **Download invoice**, **View billing** | `checkout.result.offline` |
| **Order submitted for approval** (organization member, from the cart — [REQ-MKT-003.8](../../02-requirements/FRD/cart-checkout/requirement.md)) | "Order submitted for approval", order reference, "Your organization admin approves orders before payment. You'll be able to pay once it's approved." | **View my orders**, **Browse products** | `checkout.result.orderSubmitted` |

If no offline bank details have been entered yet, the bank block shows "Bank details will be on your invoice." and no Copy buttons.

## States and behaviour
| State | Behaviour |
|---|---|
| Loading | Skeletons for both columns, matching the final layout |
| Error | Backend message shown as-is with **Retry** |
| Not found / not the owner | "This checkout could not be found." with **View billing** |
| Invoice already PAID | Complete step, success view (no second payment — FRD BR-4) |
| Session expired (401) | `/login?returnTo=` this checkout URL |

- The step is kept in the URL (`&step=payment`), so browser Back returns to the previous step without losing entered details.
- Toasts for success and failure ([billing-ui-standards.md](billing-ui-standards.md#feedback)).

## Accessibility
- Payment tiles are a **radio group** (`role="radiogroup"`, each tile `role="radio"` with `aria-checked`): arrow keys move between enabled tiles, Space/Enter selects; disabled tiles have `aria-disabled` and their tooltip text is reachable by keyboard. Saved cards are a nested radio group.
- The breadcrumb is a `nav` with an ordered list; the current step has `aria-current="step"`.
- Every logo has alt text with the brand name (for example "Visa"); decorative icons next to a visible label are `aria-hidden`; the background curves are `aria-hidden`.
- The Amount due badge has an accessible name ("Amount due ₹105.00"), not just the drawn circle.
- Payment status changes are announced with `aria-live`.
- Visible labels; errors linked with `aria-describedby`; colour is never the only signal (the selected tile also shows a check mark).
- Read-only card placeholders are not in the tab order.
- axe tests for every state (REQ-BIL-001 AC-30, AC-31–AC-35).

## i18n
Every string in `frontend/src/i18n/locales/en.json` and `es.json` under `checkout.*` (new keys: `checkout.review.*`, `checkout.payment.amountDue`, `checkout.option.netbanking`, `checkout.option.wallets`, `checkout.savedCards.*`, `checkout.footer.*`, `checkout.result.orderSubmitted`). Amounts and dates use the platform formatting.

## API used
[billing-payments.md](../../06-api/api-requirements/billing-payments.md): `GET …/billing/checkout`, `PUT …/billing/details`, `GET …/billing/payment-methods`, `DELETE …/billing/payment-methods/{id}`, `POST …/billing/invoices/{id}/payments`, `POST …/billing/payments/{id}/confirm`, `POST …/billing/invoices/{id}/offline`. From the cart: [cart-checkout.md](../../06-api/api-requirements/cart-checkout.md) `POST /me/cart/checkout`.
