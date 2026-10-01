# Screen: Pay invoice (dialog and result states)

> **Superseded ([C55](../../01-business/roadmap/open-decisions.md#c55), built 2026-10-01):** **Pay** on an OPEN invoice now opens the [Checkout payment](checkout-payment.md) screen (`/checkout?invoiceId=…`); this dialog was removed. Kept as the record of the earlier behaviour.

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
