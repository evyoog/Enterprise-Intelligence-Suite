# Screen: Pay invoice (dialog and result states)

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.5–.7](../../02-requirements/FRD/billing-payments/requirement.md) |
| Opened from | [Billing](billing.md) → **Pay** on an OPEN invoice |
| Permissions | Same as Billing |

## Step 1 — Confirm (EIS dialog)
Read-only summary: invoice number, billing period, plan, subtotal, tax, **total**, currency, bill-to name. Option: **Pay with** the default saved method (if any) or **another method**. Buttons: **Pay {total}** (disabled when the gateway is not configured), **Cancel**.

## Step 2 — Razorpay Checkout
Opens with the Razorpay order created by the backend for this invoice. Card and UPI entry happen only there ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## Step 3 — Result
| State | When | Message | Actions | i18n key |
|---|---|---|---|---|
| Processing | Checkout returned; backend verifying | "Confirming your payment…" (spinner, no action) | — | `billing.pay.processing` |
| Success | Signature verified, payment captured | "Payment received. Invoice {number} is paid." | **Download receipt**, **Close** | `billing.pay.success` |
| Pending | Result not yet confirmed (awaiting webhook) | "Your payment is being confirmed. This page updates automatically." | **Close** | `billing.pay.pending` |
| Failed | Razorpay reported failure | "Payment failed: {reason}." | **Retry payment**, **Close** | `billing.pay.failed` |
| Cancelled | Customer closed checkout | "Payment was cancelled. The invoice is still open." | **Retry payment**, **Close** | `billing.pay.cancelled` |
| Not configured | Gateway credentials missing | "Online payments are not available yet." | **Close** | `billing.gateway.notConfigured` |
