# Screen: Payment gateway

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.13, .14](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing/payment-gateway` |
| Permissions | `MANAGE_BILLING` (platform ADMIN) |
| Rule | **Read-only.** Credentials are set only in the common secrets file ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)); this screen never shows or edits a secret |

## Fields (all read-only)
| Field | Shown as | Source |
|---|---|---|
| Provider | "Razorpay" | Fixed ([C46](../../01-business/roadmap/open-decisions.md#c46)) |
| Status | Chip: **Not configured** (any credential missing) or **Configured** | Derived |
| Mode | Test or Live, derived from the key ID prefix (`rzp_test_` / `rzp_live_`) | `RAZORPAY_KEY_ID` |
| Key ID | Masked, for example `rzp_live_••••••1234`; "Not set" when missing | `RAZORPAY_KEY_ID` |
| Key secret | "Set" / "Not set" (never the value) | `RAZORPAY_KEY_SECRET` |
| Webhook secret | "Set" / "Not set" (never the value) | `RAZORPAY_WEBHOOK_SECRET` |
| Webhook URL | Full URL of the EIS webhook endpoint, with a **Copy** button, and the note "Register this URL in the Razorpay dashboard with the events payment.captured, payment.failed and refund.processed." | Backend URL + webhook path |
| Last webhook received | Date, time and event type, or "Never" | Stored webhook events |

## Actions
**Test connection** (enabled only when Configured) makes one harmless authenticated call to Razorpay and shows **Connected** or the error returned.

## Not-configured guidance
When Status is Not configured, the screen shows: "Add the Razorpay credentials to the common secrets file, then restart the backend. See [09-integrations/razorpay.md](../../09-integrations/razorpay.md)."
