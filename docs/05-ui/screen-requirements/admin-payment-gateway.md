# Screen: Payment gateway

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.13, .14](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing/payment-gateway` |
| Permissions | `MANAGE_BILLING` (platform ADMIN) |
| Rule | **Read-only.** Credentials are set only in the common secrets file ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)); this screen never shows or edits a secret |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## Status card
A prominent card at the top: **Configured** or **Not configured** (status-chip styling, [billing-ui-standards.md](billing-ui-standards.md#status-chips)).

## Setup checklist
A tick (configured) or cross (missing) per item:
- Key ID set
- Key secret set
- Webhook secret set
- Webhook received at least once

## Fields (all read-only)
| Field | Shown as | Source |
|---|---|---|
| Provider | "Razorpay" | Fixed ([C46](../../01-business/roadmap/open-decisions.md#c46)) |
| Mode | Test or Live, derived from the key ID prefix (`rzp_test_` / `rzp_live_`) | `RAZORPAY_KEY_ID` |
| Key ID | Masked, for example `rzp_live_••••••1234`; "Not set" when missing | `RAZORPAY_KEY_ID` |
| Key secret | "Set" / "Not set" (never the value) | `RAZORPAY_KEY_SECRET` |
| Webhook secret | "Set" / "Not set" (never the value) | `RAZORPAY_WEBHOOK_SECRET` |
| Webhook URL | Full URL of the EIS webhook endpoint, with a **Copy** button | Backend URL + webhook path |
| Last webhook received | Date, time and event type, or "Never" | Stored webhook events |

## Numbered setup guide
Shown whenever Status is Not configured:
1. Get your keys from the Razorpay dashboard.
2. Add them to `config/secrets.env` (never `application.yml` — [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)).
3. Restart the backend.
4. Register the webhook URL above in the Razorpay dashboard, with the events payment.captured, payment.failed and refund.processed.
5. Run **Test connection** below to confirm.

See also [09-integrations/razorpay.md](../../09-integrations/razorpay.md).

## Actions
**Test connection** (enabled only when Configured) makes one harmless authenticated call to Razorpay and shows the result inline — **Connected** (with the time of the check) or the error returned.

> **C60 (2026-10-01):** this screen and Billing settings → Razorpay gateway share one panel (`RazorpayGatewayPanel`): credential tiles (Present / Missing, key ID masked), Test / Live, webhook URL with Copy, last webhook, Test connection, and the steps to add or rotate the keys in `config/secrets.env`. No key is entered or shown ([admin-billing-settings.md](admin-billing-settings.md#tab-4--razorpay-gateway-credentials)).
