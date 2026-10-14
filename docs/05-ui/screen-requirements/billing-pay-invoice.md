# Screen: Pay invoice (dialog and result states)

> **Superseded ([C55](../../01-business/roadmap/open-decisions.md#c55), built 2026-10-01):** **Pay** on an OPEN invoice now opens the [Checkout payment](checkout-payment.md) screen (`/checkout?invoiceId=…`); this dialog was removed. Kept as the record of the earlier behaviour.

> **Amended by [C97](../../01-business/roadmap/open-decisions.md#c97) (2026-10-13), for the live checkout screen:**
> - **Gateway-neutral wording (BI-8).** Wording that names Razorpay in the steps below is the Razorpay implementation. The checkout reads `gatewayCode` and `gatewayName` from its summary: the footer shows the Razorpay logo for Razorpay and the gateway's name for any other gateway; the messages "Online payments are not available yet." and "Confirming your payment…" never name a gateway. The window the customer pays in belongs to the active gateway.
> - **Consent links (BI-7).** The terms checkbox reads "I agree to the **Terms & Conditions**, **Refund policy** and **Privacy policy**." Each name is a link (opens in a new tab, `rel="noopener noreferrer"`) to the page set in Billing settings; one not set is plain text. Clicking a link does not tick or untick the box. Keys: `checkout.consentLead`, `checkout.consentTerms`, `checkout.consentRefund`, `checkout.consentAnd`, `checkout.consentPrivacy`.
> - **Purchase-order field (BI-9).** On the Payment step, an optional text field **"Purchase order number (optional)"** with help "Printed on the invoice. You can leave it empty." It saves when the field loses focus (`PUT …/invoices/{id}/purchase-order`), accepts up to 50 letters, digits, spaces and `. / _ -`, shows "Use up to 50 letters, digits, spaces or . / _ -" for anything else and does not call the server; a server refusal (for example the invoice is already paid) shows under the field. It never blocks Pay. Keys: `checkout.purchaseOrder.*`. Shown only when there is an invoice.
> - **Pay by invoice (BI-4).** The tile appears only for an organization the platform administrator enabled; an individual never sees it.
> - **Started purchase (BI-1).** After a card, UPI, netbanking or wallet payment is confirmed, the purchase is active when the result shows; Pay by invoice starts it at once. See [my-subscriptions.md](my-subscriptions.md) for the Pending status shown before payment.
> **Amended by [C98](../../01-business/roadmap/open-decisions.md#c98) (2026-10-14).** The gateway is chosen by the backend for each payment (default first, by currency and method), not by the browser; the footer names the gateway the backend would use first, and a payment routed to a provider the screen cannot open shows an error and charges nothing. See [Gateway chosen by the backend](checkout-payment.md#gateway-chosen-by-the-backend-c98).
> The full description of the screen is [checkout-payment.md](checkout-payment.md).

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.5–.7](../../02-requirements/FRD/billing-payments/requirement.md) |
| Opened from | [Billing](billing.md) → **Pay** on an OPEN invoice |
| Permissions | Same as Billing |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Three-step progress indicator (Review → Pay securely → Confirmation) shown throughout.

## Step 1 — Review (EIS dialog)
An order-summary card: invoice number, billing period, plan, subtotal, **one line per tax** (name, rate, amount — [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md)), **total** in large type, currency, bill-to name. A method selector lists saved payment methods as the same card-style tiles used in the Billing screen, with the default method preselected, plus an **another method** option. Buttons: **Pay {total}** (disabled when the gateway is not configured), **Cancel**.

## Step 2 — Pay securely (Razorpay Checkout)
Opens with the Razorpay order created by the backend for this invoice. Card and UPI entry happen only there ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## Step 3 — Confirmation
| State | When | Message | Actions | i18n key |
|---|---|---|---|---|
| Processing | Checkout returned; backend verifying | "Confirming your payment…" (spinner, no action) | — | `billing.pay.processing` |
| Pending | Result not yet confirmed (awaiting webhook); polls the payment's status every 5 seconds (interval Not specified by any source — chosen as a reasonable balance between promptness and load) | "Your payment is being confirmed. This page updates automatically." Status updates announced via `aria-live="polite"` | **Close** | `billing.pay.pending` |
| Success | Signature verified, payment captured | A green check icon, "Payment received. Invoice {number} is paid." | **Download receipt**, **Back to invoices** | `billing.pay.success` |
| Failed | Razorpay reported failure | "Payment failed: {reason}." | **Retry payment**, **Close** | `billing.pay.failed` |
| Cancelled | Customer closed checkout | "Payment was cancelled. The invoice is still open." | **Retry payment**, **Close** | `billing.pay.cancelled` |
| Not configured | Gateway credentials missing | "Online payments are not available yet." | **Close** | `billing.gateway.notConfigured` |
