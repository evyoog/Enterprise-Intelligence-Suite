# Screen: Billing settings (offline bank details)

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.21](../../02-requirements/FRD/billing-payments/requirement.md) |
| Decision | [C55](../../01-business/roadmap/open-decisions.md#c55) |
| Route | `/admin/billing/settings` |
| Sidebar | Admin → Billing → **Billing settings** (alongside Invoices & payments, Payment gateway, Tax) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Why a separate screen | [Payment gateway](admin-payment-gateway.md) is read-only and shows only whether secrets-file credentials are set. Bank details are editable and are **not** secrets — they are printed on every offline invoice — so they do not belong in the secrets file ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)) or on that screen |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## Offline payment bank details
Shown on offline invoices and on the checkout's offline result ([checkout-payment.md](checkout-payment.md#step-3--complete)). Field list to confirm (FRD Open question 11).

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Account name | Text | Yes | Non-blank, max 200 | `admin.billingSettings.accountName` |
| Bank name | Text | Yes | Non-blank, max 200 | `admin.billingSettings.bankName` |
| Account number | Text | Yes | Non-blank, max 34 | `admin.billingSettings.accountNumber` |
| IFSC | Text | No | 11 characters if given (format check Not specified) | `admin.billingSettings.ifsc` |
| SWIFT / BIC | Text | No | 8 or 11 characters if given (format check Not specified) | `admin.billingSettings.swift` |

Buttons: **Save** (disabled until changed), **Cancel**. An **"unsaved changes"** warning appears when leaving with edits. Last updated time and by whom are shown under the form.

## States
Loading skeleton; empty state "No bank details yet. Offline invoices will say 'Bank details will be on your invoice.' until you add them."; backend errors as-is with **Retry**; toast on save.

## Accessibility
Visible labels, errors linked to inputs, keyboard-reachable form; axe test (FRD AC-30).
