# Screen: Billing settings

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.21](../../02-requirements/FRD/billing-payments/requirement.md), extended by [C60](../../01-business/roadmap/open-decisions.md#c60); sprint 2026.4.3 ([C97](../../01-business/roadmap/open-decisions.md#c97)): REQ-BIL-001.28 (pay-by-invoice organizations), .30 (policy links), .31 (payment gateways, replaced by [REQ-BIL-003](../../02-requirements/FRD/payment-gateways/requirement.md), [C98](../../01-business/roadmap/open-decisions.md#c98)) |
| Decisions | [C55](../../01-business/roadmap/open-decisions.md#c55) (offline bank details), [C60](../../01-business/roadmap/open-decisions.md#c60) (full billing settings, Razorpay credentials view, corporate design) |
| Route | `/admin/billing/settings` (`?tab=business` · `offline` · `methods` · `gateway` · `reminders`) |
| Sidebar | Admin → Billing → **Billing settings** (alongside Invoices & payments and Payment gateway) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Built | 2026-10-01 — `frontend/src/pages/admin/AdminBillingSettingsPage.tsx` |
| Secrets | **Never shown.** The **Payment gateways** tab lets `MANAGE_BILLING` enter and rotate a gateway's credentials ([C98](../../01-business/roadmap/open-decisions.md#c98); write-only, encrypted at rest, see [admin-payment-gateway.md](admin-payment-gateway.md)); no other tab handles a secret. Credentials in `config/secrets.env` ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)) keep working |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Corporate design: [C60](../../01-business/roadmap/open-decisions.md#c60) (page header with icon and brand stripe, accent-coloured sections).

## Layout
- **Page header:** Settings icon (violet), eyebrow "Billing", title, subtitle.
- **Overview tiles** (4, clickable — they open their tab): Business & invoicing (blue: legal name, Complete / Needs details), Offline payments (teal: bank name, Ready / Needs details), Payment methods (violet: "n of 5 on"), Payment gateways (amber: "{active} of {implemented} active" from `GET /admin/billing/gateways/summary`, with the chip Configured when at least one gateway is active, otherwise None active).
- **Tabs** with icons; the tab is kept in the URL. The fifth tab, **Renewal reminders**, is specified in [renewal-reminders.md](renewal-reminders.md). The **Payment gateways** tab (formerly "Razorpay gateway") is provider-independent since [C98](../../01-business/roadmap/open-decisions.md#c98): it embeds the same list, dialogs and overview as the [Payment Gateways](admin-payment-gateway.md) screen.
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

| Policy pages ([C97](../../01-business/roadmap/open-decisions.md#c97) BI-7) | Terms & Conditions page, Privacy policy page, Refund policy page | No | Each blank or starting with `http://` or `https://`, max 500; placeholders `https://www.evyoog.com/terms`, `…/privacy`, `…/refunds` | `…f.termsUrl`, `…f.privacyUrl`, `…f.refundPolicyUrl` |

The Policy pages section (rose, document icon) says the pages are on the evyoog.com website and EIS links to them. The checkout consent sentence links to the three pages and the invoice document prints them (each only if set). The URLs themselves are not decided yet (Not specified): until the administrator enters them the consent shows plain text.

The invoice number format shown in the preview follows the number format of [REQ-BIL-001.26](../../02-requirements/FRD/billing-payments/requirement.md) (`EIS/26-27/000123`); the defaults for a new installation are prefix `EIS` and payment terms 15 days.

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
| Online payment methods | Cards, UPI, Netbanking, Wallets (switch cards) | Hidden from the checkout when off; the start-payment API refuses an off method. While no gateway is active each shows "Shown to customers once the Razorpay keys are set" (`admin.billingSettings.h.needsGateway`; the wording still names Razorpay). Which of the switched-on methods checkout actually offers also depends on the active gateways ([REQ-BIL-003.11](../../02-requirements/FRD/payment-gateways/requirement.md)) |
| Offline payment | Pay by invoice | The **global switch**. Off: the tile is hidden for everyone and the API refuses the offline route. On: only the organizations enabled in the next section may use it |
| Organizations that can pay by invoice ([C97](../../01-business/roadmap/open-decisions.md#c97) BI-4) | List and search, see below | Individual customers never can |
| Razorpay Checkout appearance | Name shown in Razorpay Checkout (max 100), Description (max 255), Checkout colour (`#RRGGBB`, with a colour picker) | Passed to Razorpay Checkout; not secrets |

**Organizations that can pay by invoice** (indigo section, building icon; title "Organizations that can pay by invoice", subtitle "Pay by invoice is only for organizations you enable here. Individual customers always pay online."):
- **Enabled organizations** (list, by name): organization name, its code and a **switch** with the accessible name "Pay by invoice for {name}". "No organization is enabled yet." when empty.
- **Find an organization:** a search box (helper "Type at least 2 letters of its name."), debounced 300 ms; it lists up to 20 matches, enabled or not, each with its switch; "No organization matches." when none.
- Each switch **saves at once** (no Save bar), is audited (`ORG_PAY_BY_INVOICE_ENABLED` / `_DISABLED`), and the enabled list refreshes. A refusal shows the backend message in an alert. An organization sees the tile at checkout only when this switch **and** the global switch are on.
- Organizations that already paid by invoice before the rule are enabled by the migration, so their open invoices stay payable.

At least one method (online or Pay by invoice) must stay on. Preview: a mock of the Razorpay window header in the chosen colour with the methods that are on.

## Tab 4 — Payment gateways
The tab renders `PaymentGatewayManagement`, **the same component as the [Payment Gateways](admin-payment-gateway.md) screen** ([C98](../../01-business/roadmap/open-decisions.md#c98), REQ-BIL-003), without that page's header. Everything in [admin-payment-gateway.md](admin-payment-gateway.md) applies here unchanged:

- the four overview statistics (Registered, Configured, Active, Need configuration);
- one card per gateway with the separate Configuration, Connection, Active and Default chips and only the valid actions (Configure, Test connection, Activate, Make default, Deactivate, View details; Edit and Remove for a gateway an administrator added), and the **Add payment gateway** button and dialog ([C99](../../01-business/roadmap/open-decisions.md#c99)); a gateway an administrator added is labelled "No payment integration" and is never operational (there is no Planned section any more);
- the **Configure** dialog generated from the provider's fields (write-only secrets, a blank keeps the stored value, environment select, tested first when the gateway is active);
- the **Details** dialog with the Overview, Webhook and Activity tabs.

The earlier C97 list (one card with Test connection and Make active) and the Razorpay credentials panel (tiles, "add or rotate the keys in `config/secrets.env`" steps, no key entry) no longer exist; their content is in the gateway card and details dialog above. The tab shows no key or secret. Related: the Payment methods tab (Tab 3) switches methods on for the whole platform, the details dialog switches them per gateway.

## States
Loading skeletons per tab; backend errors as-is with **Retry**; success toast "Settings saved."; field errors under each field after the first save attempt.

## Accessibility
Visible labels; errors in helper text; switches have accessible names; overview tiles are buttons with `aria-pressed`; tabs are linked to their panel; previews are labelled regions; the save bar status is announced (`role="status"`); axe tests for each tab.

## API used
[billing-payments.md](../../06-api/api-requirements/billing-payments.md): `GET/PUT /admin/billing/settings/business`, `GET/PUT /admin/billing/settings/offline`, `GET/PUT /admin/billing/settings/payment-methods`, `GET/PUT …/settings/pay-by-invoice/organizations[/{id}]`, `GET /admin/billing/gateways/summary` (overview tile) and the gateway endpoints of [payment-gateways.md](../../06-api/api-requirements/payment-gateways.md) through the embedded list. The earlier `GET /admin/billing/gateway` and `POST /admin/billing/gateway/test` are no longer called by this screen.

## Built (sprint 2026.4.3)
Policy links and the organizations panel (`frontend/src/components/settings/PayByInvoiceOrganizations.tsx`) in `AdminBillingSettingsPage.tsx`, 2026-10-13. The gateways tab was rebuilt on 2026-10-14 ([C98](../../01-business/roadmap/open-decisions.md#c98)): it embeds `frontend/src/components/payments/PaymentGatewayManagement.tsx` (the C97 `PaymentGatewaysPanel.tsx` was removed). Tests: `AdminBillingSettingsPage.test.tsx` (including "shows the payment gateways tab with the same list as the Payment Gateways page (C98)").
