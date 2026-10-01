# Screen: Checkout payment

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.18, .19](../../02-requirements/FRD/billing-payments/requirement.md) |
| Decision | [C55](../../01-business/roadmap/open-decisions.md#c55) |
| Route | `/checkout?subscriptionId=…` or `/checkout?invoiceId=…`, inside the signed-in tool frame ([application-layout.md](application-layout.md)) |
| Entry points | **Subscribe** on a paid plan (product detail page, catalog); **Pay** on an OPEN invoice (Billing → Invoices); the invoice issued after an organization order is approved |
| Permissions | The customer who owns the subscription/invoice, or an organization billing user (FRD Open question 5). Anyone else: 404 |
| Replaces (Phase 2) | The C48 page `/checkout/:productId` and the Billing "Pay invoice" dialog ([billing-pay-invoice.md](billing-pay-invoice.md)) — one checkout for every payment ([C44](../../01-business/roadmap/open-decisions.md#c44): one screen with steps, not one screen per function) |
| Card data | [BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md) — see [Card panel](#card-panel-approach-br-bil-001) |

Shared presentation rules (theme, status chips, money formatting, responsive, loading/empty/error, toasts, accessibility, i18n, gateway banner): [billing-ui-standards.md](billing-ui-standards.md). Colours come only from the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45), `frontend/src/theme.ts`), in light and dark mode.

## Page structure

### Desktop (≥ 1024 px)
- **Stepper**, centred at the top: **1 Billing details → 2 Payment → 3 Complete**, joined by thin lines. Active step: filled primary circle with its number and a bold label. Completed step: check mark. Upcoming step: muted grey.
- **Two columns** below:
  - **Left — payment panel**, about 60 % width: card on `background.paper`, rounded corners, subtle `divider` border.
  - **Right — order summary**, about 40 % width: rounded card with a low-opacity tint of the theme's primary/secondary gradient and a subtle border.
- Spacing: 24 px padding inside cards, 16 px gaps.

### Tablet (600–1023 px)
Same two columns, narrower; the order summary may move below the payment panel when the content does not fit. Not otherwise specified.

### Mobile (< 600 px)
- One column.
- The order summary collapses into an expandable **"Order summary · {total}"** bar at the top.
- The Pay / Generate invoice button is sticky at the bottom of the viewport.

## Step 1 — Billing details
Pre-filled from the customer's billing details (REQ-BIL-001.1).

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

Same validation as [billing.md](billing.md) tab 5. Buttons: **Continue to payment** (primary; saves the details), **Back**.

If the details are already complete, the step shows a **read-only summary** with an **Edit** link, and the customer can go straight to Payment.

## Step 2 — Payment (left panel)
Heading **"Select payment option"** (`checkout.payment.title`) with a small lock icon and the subtitle **"All transactions are secure and encrypted."** (`checkout.payment.subtitle`).

### Payment options
A vertical list of selectable option cards: radio on the left, title and short description, method icons on the right. The selected card has a primary-coloured border and a light primary tint, and expands to show its panel; the others stay collapsed.

| Option | i18n key | Right-side icons | Expanded panel |
|---|---|---|---|
| **Credit / debit card** | `checkout.option.card` | Accepted card network marks — official acceptance-mark assets used according to each network's brand guidelines, or text labels (for example "Visa", "Mastercard", "RuPay") if licensed assets are not available. Never copied from the reference image. | "Pay securely with your card." Placeholder fields in the reference layout (see [Card panel](#card-panel-approach-br-bil-001)): **Card number** (full width, placeholder `1234 1234 1234 1234`, card-type icon on the right); a row of three: **Name on card**, **Expiry date** (`MM / YY`), **CVV** (with a help icon: "3 digits on the back, 4 on the front for Amex"). Checkbox: **"Save this card securely with Razorpay for future payments"** — unticked by default; consent per BR-BIL-001 and FRD BR-9. |
| **UPI** | `checkout.option.upi` | UPI and UPI app marks (same asset rule) | "Pay with any UPI app — Google Pay, PhonePe, Paytm, BHIM." UPI ID entry or pay with a UPI app (QR / intent) happens in Razorpay's window, as Razorpay supports it. |
| **Other online methods** | `checkout.option.other` | Generic bank/wallet icon | "Netbanking, wallets and other methods through Razorpay's secure window." Which methods: Not specified (FRD Open question 14). |
| **Pay by invoice (offline)** | `checkout.option.offline` | Invoice/document icon | "We'll issue an invoice now. Pay by bank transfer, NEFT/RTGS or cheque, quoting the invoice number." Shows the payment terms (Not specified until FRD Open question 10 is decided — "Payment terms will be shown on the invoice" until then) and the billing email the invoice will be sent to. **Shown only if the customer is allowed to use it** (FRD Open question 9). |

### Card panel approach (BR-BIL-001)
Mandatory rule: no EIS frontend state, API, log, database column or analytics event may ever hold a full card number, CVV or expiry entered by the customer.

- **Approach used: placeholder fields + Razorpay Checkout** ([C55](../../01-business/roadmap/open-decisions.md#c55)). No Razorpay product offering provider-hosted, embeddable card fields could be confirmed: Razorpay's Custom UI integration has the merchant's own page collect the card values and pass them to `razorpay.createPayment`, which BR-BIL-001 forbids. So:
  - The card number, name, expiry and CVV fields in the panel are **read-only placeholders** (`readOnly`, not focusable for typing, `aria-hidden` on the decorative values, with a visible note "You'll enter your card details in Razorpay's secure window."). They give the reference-design look only; they never hold customer input.
  - **Pay** opens Razorpay Standard Checkout with the **card** method preselected for the invoice total. The customer types the card there.
  - Card validation messages (invalid number, expired card, wrong CVV) are shown by Razorpay inside its window. A failure returned to EIS is shown on step 3 using the provider's reason, in the theme's error style.
  - The "Save this card" checkbox value (true/false only) is passed to the backend as consent, as for [Add payment method](billing-add-payment-method.md).
- **To re-verify before Phase 2** (FRD Open question 15): razorpay.com could not be reached from the build environment. If Razorpay does offer provider-hosted card iframes, the panel renders them in the same layout instead of placeholders, and EIS still never reads their values.

### Pay button and consent
- Full-width, large primary button: **"Pay {total}"** (`checkout.pay`, for example "Pay ₹1,220.80"), total formatted with the platform currency settings. When **Pay by invoice** is selected the label is **"Generate invoice"** (`checkout.generateInvoice`).
- Below it, a required checkbox, unticked: **"By clicking this, I agree to the Terms & Conditions and Privacy Policy"** (`checkout.consent`), with both as links. URLs: Not specified — no legal routes exist in the app today (FRD Open question 12).
- The button is disabled until the consent box is ticked and an available option is selected.
- Double-click protection: the button is disabled and shows a spinner while the request is in progress.

### Gateway not configured
Card, UPI and Other online methods are shown **disabled** with the shared banner **"Online payments are not available yet."** (`billing.gateway.notConfigured`, [billing-ui-standards.md](billing-ui-standards.md#gateway-banner)). Pay by invoice stays available if the customer is allowed to use it. If it is not allowed either, the panel shows the banner and no enabled option; the Pay button stays disabled.

## Order summary (right panel)
- Heading **"Your order"** (`checkout.summary.title`) with an item-count badge, for example "(2)".
- **Item rows**, each in a small card on `background.paper`: product image or icon (from the catalog; generic product icon if none), product name, plan name, billing period (monthly / yearly) and term dates, amount. **"Qty: 1"** is shown only if a quantity exists on the subscription — seat/quantity rules are undecided (D14) and are not invented.
- **No coupon field** ([C50](../../01-business/roadmap/open-decisions.md#c50): promotions later).
- **Order summary block:** Subtotal; one line per tax with its name and rate, for example **"GST (18 %)"**, from [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) — if the region has no rule, **"Tax: none for this region"**; no shipping row (subscription software); a divider; **Total** in large, bold type. Currency and formatting from the platform settings.
- Trust line at the bottom: lock icon + **"Payments are processed securely by Razorpay."** (`checkout.summary.trust`).
- The order summary stays visible on every step.

## Step 3 — Complete
Focus moves to the result heading. Status changes are announced with `aria-live="polite"`.

| Result | Content | Actions | i18n key |
|---|---|---|---|
| **Online payment successful** | Large success check, "Payment successful", invoice number, amount paid, method (for example "Visa •••• 4242" or "UPI" — display fields only, from Razorpay), date | **Download receipt**, **Download invoice**, **Go to my products** (or the subscription), **View billing** | `checkout.result.success` |
| **Online payment pending** | Spinner, "We're confirming your payment". Polls the payment status every **5 seconds** for up to **2 minutes** (interval Not specified by any source — chosen as a reasonable balance; after 2 minutes: "This is taking longer than usual. We'll email you when it's confirmed." with **View billing**) | — | `checkout.result.pending` |
| **Online payment failed / cancelled** | Error icon, the provider's reason, "Your invoice is still open." | **Try again** (back to step 2 with the same option), **Choose another method** | `checkout.result.failed` |
| **Invoice generated (offline)** | Document icon, "Invoice generated", invoice number, total, due date (Not specified until FRD Open question 10), **bank details block** (account name, bank, account number, IFSC, SWIFT/BIC — from [admin-billing-settings.md](admin-billing-settings.md)), each with a **Copy** button; "Quote invoice number {number} as the payment reference"; "We've emailed the invoice to {billing email}"; "Your subscription status: {status}" (activation rule: FRD Open question 3) | **Download invoice**, **View billing** | `checkout.result.offline` |

If offline bank details have not been entered by an admin yet, the bank block shows "Bank details will be on your invoice." and no Copy buttons (the invoice is still generated).

## States and behaviour
| State | Behaviour |
|---|---|
| Loading | Skeletons for both columns, matching the final layout |
| Error | Backend message shown as-is with **Retry** |
| Not found / not the owner | 404 message: "This checkout could not be found." with **View billing** |
| Invoice already PAID | Step 3 success view for that invoice (no second payment — FRD BR-4) |
| Session expired (401) | Sends to `/login?returnTo=` this checkout URL (same rule as the C48 page) |

- Every step change keeps the order summary visible.
- Browser Back returns to the previous step without losing entered billing details (step is kept in the URL, for example `&step=payment`).
- Toasts for success and failure ([billing-ui-standards.md](billing-ui-standards.md#feedback)).

## Accessibility
- Payment options are a proper **radio group** (`role="radiogroup"`): arrow keys move between options, Space/Enter selects, the expanded panel is announced (`aria-expanded`, `aria-controls`).
- The stepper marks the current step with `aria-current="step"`.
- Every input has a visible label; errors are linked to their inputs (`aria-describedby`); colour is never the only signal.
- Read-only card placeholders are not in the tab order.
- axe test for the screen (FRD AC-30).

## i18n
Every string in `frontend/src/i18n/locales/en.json` and `es.json` under the `checkout.*` prefix (existing `checkout.*` keys from C48 are reused where the text is the same). Amounts and dates use the platform's currency and date formatting.

## API used
[billing-payments.md](../../06-api/api-requirements/billing-payments.md): `GET …/billing/checkout`, `PUT …/billing/details`, `POST …/billing/invoices/{id}/payments`, `POST …/billing/payments/{id}/confirm`, `POST …/billing/invoices/{id}/offline`.
