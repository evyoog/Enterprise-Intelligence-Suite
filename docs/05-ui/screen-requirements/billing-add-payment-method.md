# Screen: Add payment method (dialog)

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.10](../../02-requirements/FRD/billing-payments/requirement.md) |
| Opened from | [Billing](billing.md) → Payment methods tab, or Overview |
| Permissions | Same as Billing |
| Rule | EIS shows **no** card-number, CVV or bank fields. Card or UPI details are entered only in Razorpay's secure window ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Three-step stepper (Choose method → Secure entry → Done) shown at the top of the dialog throughout.

## Step 1 — Choose a method (EIS dialog)
Method choice is presented as large selectable cards, one per method, not radio buttons:

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Method type | Selectable cards: Card, UPI | Yes | One selected | `billing.addMethod.type` |
| Consent to save (card) | Checkbox: "I agree to securely save this card with Razorpay for future payments" | Yes, for cards | Must be ticked to continue | `billing.addMethod.consent` |
| Set as default | Checkbox | No | — | `billing.addMethod.makeDefault` |

A reassurance panel with a lock icon sits below the method cards: **"Card details are entered securely in Razorpay's window. EIS never sees or stores your full card number."** The consent checkbox label links to a short explanation of what a saved card means (tokenized reference only, per [BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

Buttons: **Continue to secure entry** (disabled until valid, or when the gateway is not configured), **Cancel**.

## Step 2 — Razorpay secure window
The Razorpay window opens over the dialog. The customer enters card or UPI details there. EIS receives only the saved-method reference and display details.

## Step 3 — Result
| State | Message | i18n key |
|---|---|---|
| Saved | "Payment method saved." Shows the new tile (network/UPI app, last 4, expiry) as it will appear in the Payment methods tab; the list refreshes | `billing.addMethod.saved` |
| Cancelled | "Nothing was saved." | `billing.addMethod.cancelled` |
| Failed | The reason returned by Razorpay | `billing.addMethod.failed` |
| Gateway not configured | "Online payments are not available yet." | `billing.gateway.notConfigured` |

## Open point
Whether saving a method requires a small verification charge, and which methods beyond card and UPI are offered: FRD Open question 7.
