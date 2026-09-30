# Integration — Razorpay (payments)

| Field | Value |
|---|---|
| Purpose | Online payments for invoices, saved payment methods, refunds |
| Decision | [C46](../01-business/roadmap/open-decisions.md#c46) — Razorpay chosen; credentials to be provided later by the product owner |
| Requirement | [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md) |
| Status | **Not configured** — no credentials yet. EIS runs in Not configured mode until they are added |

## Credentials (in the common secrets file only)
Set in `config/secrets.env` ([BR-SEC-001](../03-business-rules/BR-SEC-001-central-secrets-file.md)); never in code, never on a screen.

| Variable | What it is | Where to get it |
|---|---|---|
| `RAZORPAY_KEY_ID` | Public key ID (`rzp_test_…` or `rzp_live_…`); the only value the browser receives | Razorpay dashboard → API keys |
| `RAZORPAY_KEY_SECRET` | Secret for server calls and payment-signature verification | Razorpay dashboard → API keys |
| `RAZORPAY_WEBHOOK_SECRET` | Secret for webhook-signature verification | Set when creating the webhook in the Razorpay dashboard |

Use test keys first; switch to live keys only after acceptance testing.

## Webhook
Register `<backend URL>/webhooks/razorpay` in the Razorpay dashboard with the events `payment.captured`, `payment.failed` and `refund.processed`. The exact URL is shown on the admin [Payment gateway](../05-ui/screen-requirements/admin-payment-gateway.md) screen.

## How EIS uses Razorpay
| Step | Razorpay feature |
|---|---|
| Pay an invoice | Create an order on the server → open Razorpay Checkout in the browser → verify the payment signature on the server |
| Save a payment method | Razorpay saved cards / tokenization with customer consent, and UPI |
| Refund | Refund API on a captured payment |
| Confirmation and reconciliation | Webhooks, plus an on-demand status fetch |

## Not decided yet
Auto-debit for renewals (e-mandate), netbanking and wallets, and international (non-INR) customers — see FRD Open questions and decisions D2, D15.
