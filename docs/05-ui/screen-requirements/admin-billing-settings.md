# Screen: Billing settings

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.21](../../02-requirements/FRD/billing-payments/requirement.md), extended by [C60](../../01-business/roadmap/open-decisions.md#c60) |
| Decisions | [C55](../../01-business/roadmap/open-decisions.md#c55) (offline bank details), [C60](../../01-business/roadmap/open-decisions.md#c60) (full billing settings, Razorpay credentials view, corporate design) |
| Route | `/admin/billing/settings` (`?tab=business` · `offline` · `methods` · `gateway`) |
| Sidebar | Admin → Billing → **Billing settings** (alongside Invoices & payments and Payment gateway) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Built | 2026-10-01 — `frontend/src/pages/admin/AdminBillingSettingsPage.tsx` |
| Secrets | **None on this screen.** Razorpay keys live only in `config/secrets.env` ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)); the Razorpay tab shows only whether each key is set |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Corporate design: [C60](../../01-business/roadmap/open-decisions.md#c60) (page header with icon and brand stripe, accent-coloured sections).

## Layout
- **Page header:** Settings icon (violet), eyebrow "Billing", title, subtitle.
- **Overview tiles** (4, clickable — they open their tab): Business & invoicing (blue: legal name, Complete / Needs details), Offline payments (teal: bank name, Ready / Needs details), Payment methods (violet: "n of 5 on"), Razorpay gateway (amber: Test / Live, Configured / Not configured).
- **Tabs** with icons; the tab is kept in the URL.
- Each form tab: accent-coloured **sections** (left colour bar, icon, title, description) on the left, a sticky **live preview** on the right (large screens), and a sticky **save bar** (unsaved-changes notice, last updated, **Discard**, **Save changes** — disabled until something changed). Leaving the browser tab with unsaved changes asks first.

## Tab 1 — Business & invoicing
Printed at the top of every invoice and receipt (the invoice issuer).

| Section | Field | Required | Validation | i18n key |
|---|---|---|---|---|
| Legal entity | Legal name | Yes | Max 200 | `admin.billingSettings.f.legalName` |
| | Trade name | No | Max 200 | `…f.tradeName` |
| | GSTIN | No | 15 characters, GSTIN format (no checksum) | `…f.gstin` |
| | PAN | No | 10 characters, PAN format | `…f.pan` |
| | CIN | No | 21 characters, CIN format | `…f.cin` |
| Registered address | Address line 1, City, State / region, Postal code, Country | Yes | Max 200 / 100 / 100 / 20 / 100 | `…f.addressLine1` … |
| | Address line 2 | No | Max 200 | `…f.addressLine2` |
| Billing contact | Billing email | Yes | Email | `…f.email` |
| | Phone | No | 6–30 of digits, spaces, + ( ) - | `…f.phone` |
| | Website | No | Starts with http:// or https:// | `…f.website` |
| Invoicing | Invoice number prefix | Yes (default `INV`) | 2–10 letters or digits; new invoices are `PREFIX-YEAR-NUMBER`, issued numbers never change | `…f.invoicePrefix` |
| | Payment terms | Yes (default 0) | 0–365 days; quick chips On receipt, Net 7/15/30/45/60/90. Due date = issue date + terms | `…f.paymentTermsDays` |
| | Invoice footer note | No | Max 500; printed at the end of invoices and receipts | `…f.invoiceFooterNote` |

Preview: the issuer block, registration chips, the next invoice number and the due rule.

## Tab 2 — Offline payments
Shown on offline invoices, in the Pay-by-invoice email and on the checkout's offline result.

| Section | Field | Required | Validation |
|---|---|---|---|
| Bank account | Account holder name, Bank name, Account number | Yes | Max 200 / 200 / 34 |
| | Branch | No | Max 200 |
| | Account type | No | Current / Savings |
| | IFSC | No | 4 letters, 0, 6 letters or digits |
| | MICR | No | 9 digits |
| International transfers | SWIFT / BIC | No | 8 or 11 letters or digits |
| | IBAN | No | Country code, check digits, 11–30 characters |
| Direct UPI transfer | UPI ID | No | `name@bank` |
| Cheque payments | Cheques payable to; Cheque mailing address | No | Max 200 / 500 |
| Accepted offline methods | Bank transfer, NEFT / RTGS, Cheque (switches) | At least one on | The admin **Record offline payment** dialog only offers accepted methods; the API refuses others |
| Payment instructions | Free text | No | Max 1000 |

Preview: "What customers see" — the bank block, accepted-method chips, cheque payee, instructions.

## Tab 3 — Payment methods
| Section | Field | Notes |
|---|---|---|
| Online payment methods | Cards, UPI, Netbanking, Wallets (switch cards) | Hidden from the checkout when off; the start-payment API refuses an off method. While Razorpay is not configured each shows "Shown to customers once the Razorpay keys are set" |
| Offline payment | Pay by invoice | Off: the tile is hidden and the API refuses the offline route (answers REQ-BIL-001 Open question 9 as an admin switch for everyone) |
| Razorpay Checkout appearance | Name shown in Razorpay Checkout (max 100), Description (max 255), Checkout colour (`#RRGGBB`, with a colour picker) | Passed to Razorpay Checkout; not secrets |

At least one method (online or Pay by invoice) must stay on. Preview: a mock of the Razorpay window header in the chosen colour with the methods that are on.

## Tab 4 — Razorpay gateway (credentials)
Same panel as the [Payment gateway](admin-payment-gateway.md) screen.

- **Razorpay credentials:** Configured / Not configured and Test / Live chips; three tiles — Key ID (masked, for example `rzp_test_••••••7890`), Key secret, Webhook secret — each **Present** (green) or **Missing** (red). **Test connection** (enabled when configured) shows the result.
- **Webhook:** the URL to register in Razorpay, with Copy; last webhook received.
- **Add or rotate the keys:** five numbered steps — generate keys in the Razorpay Dashboard; put them in `config/secrets.env` (`RAZORPAY_KEY_ID=`, `RAZORPAY_KEY_SECRET=`, `RAZORPAY_WEBHOOK_SECRET=`, each with Copy); register the webhook; restart the backend; test the connection.
- Security note: keys are never typed into, stored by or shown on this screen (BR-SEC-001 rules 1 and 5). Entering keys on a screen would need BR-SEC-001 to change first (encrypted storage, rotation, audit) — not built.

## States
Loading skeletons per tab; backend errors as-is with **Retry**; success toast "Settings saved."; field errors under each field after the first save attempt.

## Accessibility
Visible labels; errors in helper text; switches have accessible names; overview tiles are buttons with `aria-pressed`; tabs are linked to their panel; previews are labelled regions; the save bar status is announced (`role="status"`); axe tests for each tab.

## API used
[billing-payments.md](../../06-api/api-requirements/billing-payments.md): `GET/PUT /admin/billing/settings/business`, `GET/PUT /admin/billing/settings/offline`, `GET/PUT /admin/billing/settings/payment-methods`, `GET /admin/billing/gateway`, `POST /admin/billing/gateway/test`.
