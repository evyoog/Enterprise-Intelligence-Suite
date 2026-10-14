# Integration — Razorpay (payments)

| Field | Value |
|---|---|
| Purpose | Online payments for invoices, saved payment methods, refunds |
| Decision | [C46](../01-business/roadmap/open-decisions.md#c46) — Razorpay chosen; credentials to be provided later by the product owner |
| Requirement | [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md); provider-independent management since [REQ-BIL-003](../02-requirements/FRD/payment-gateways/requirement.md) ([C98](../01-business/roadmap/open-decisions.md#c98)), see [payment-gateways.md](payment-gateways.md) |
| Status | **Implemented** as the `RazorpayGateway` adapter, the only real payment integration. **Not configured** until credentials are supplied (secrets file or the Payment Gateways screen); EIS then runs in "Payment gateway not configured" mode. A real connection test and a real webhook delivery have not been run by the automated tests |

## Credentials: the secrets file or the Payment Gateways screen
Razorpay credentials can come from two places ([C98](../01-business/roadmap/open-decisions.md#c98)). **A value stored on the screen overrides the secrets-file value, field by field.**

1. **The common secrets file (unchanged).** Set in `config/secrets.env` ([BR-SEC-001](../03-business-rules/BR-SEC-001-central-secrets-file.md)) and loaded into the environment; deployed environments use their secret store. Existing installations need no action.
2. **The Payment Gateways screen** (Platform Admin → Billing → Payment gateways → Razorpay → Configure; `MANAGE_BILLING`). Values are stored **encrypted** (AES-256-GCM) and are **write-only**: they are never shown again, only "Set (stored here)" and, for the Key ID, a masked form. This needs the master key `PAYMENT_GATEWAY_ENCRYPTION_KEY` to be set on the server first, otherwise saving is refused (see [payment-gateways.md](payment-gateways.md#security-model)).

| Field (screen) | Variable (secrets file) | What it is | Where to get it |
|---|---|---|---|
| Key ID (`keyId`) | `RAZORPAY_KEY_ID` | Public key ID (`rzp_test_…` or `rzp_live_…`); the only value the browser receives (as `keyId` in the payment response) | Razorpay dashboard → API keys |
| Key secret (`keySecret`) | `RAZORPAY_KEY_SECRET` | Secret for server calls and payment-signature verification | Razorpay dashboard → API keys |
| Webhook secret (`webhookSecret`) | `RAZORPAY_WEBHOOK_SECRET` | Secret for webhook-signature verification | Set when creating the webhook in the Razorpay dashboard |

The **environment** (Test or Live) is chosen on the screen and **must match the Key ID prefix**: `rzp_test_` needs Test, `rzp_live_` needs Live; a mismatch is refused when saving and reported by Test connection ([BR-14](../02-requirements/FRD/payment-gateways/business-rules.md)). A configuration that predates the screen has no environment chosen until one is saved.

Changing the credentials of an active Razorpay gateway is tested first; if Razorpay refuses the new ones nothing is saved and the old ones keep working (BR-9). After the C98 migration Razorpay stays **active and default**. Use test keys first; switch to live keys only after acceptance testing.

## Webhook
Register `<backend URL>/webhooks/razorpay` in the Razorpay dashboard with the events `payment.captured`, `payment.failed` and `refund.processed`. **The path is unchanged** by C98 (the endpoint is now `POST /webhooks/{provider}` for every provider, and `razorpay` is one of them). The backend URL is `app.backend-url` (environment variable `BACKEND_URL`, in production `https://eis.evyoog.com/api`, so the full URL is `https://eis.evyoog.com/api/webhooks/razorpay`). The exact URL, the set-up steps and the last event are shown on **Payment Gateways → Razorpay → View details → Webhook** ([admin-payment-gateway.md](../05-ui/screen-requirements/admin-payment-gateway.md)).

The signature is the `X-Razorpay-Signature` header, checked against the webhook secret and the raw body. A bad signature is rejected with 400 and counted on the gateway; each event id is processed once, and events are accepted even if the gateway is deactivated ([BR-12, BR-13](../02-requirements/FRD/payment-gateways/business-rules.md)).

## How EIS uses Razorpay
| Step | Razorpay feature |
|---|---|
| Pay an invoice | The backend routes the payment to Razorpay (when it is active, configured and takes the currency and method), creates an order on the server → Razorpay Checkout opens in the browser → the payment signature is verified on the server. The payment records `provider = RAZORPAY` and is later confirmed, reconciled and refunded through Razorpay whatever is default |
| Save a payment method | Razorpay saved cards / tokenization with customer consent, and UPI. **Razorpay only**: other providers' tokens are not built |
| Refund | Refund API on a captured payment |
| Confirmation and reconciliation | Webhooks, plus an on-demand status fetch |

## Not decided yet
Auto-debit for renewals (e-mandate), netbanking and wallets, and international (non-INR) customers — see FRD Open questions and decisions D2, D15.
