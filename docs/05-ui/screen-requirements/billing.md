# Screen: Billing

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/billing` (signed-in tool frame, [application-layout.md](application-layout.md)) |
| Sidebar | Workspace → **Billing**, shown to individual customers and to organization billing users (permission: FRD Open question 5) |
| Permissions | Own billing only (individual) or the organization's billing (organization billing user). Others: route hidden; API returns 404 |
| Layout | One screen with five tabs, following the C44 rule of grouping related functions instead of one screen per function |

Shared presentation rules (theme, `<DataTable>`/`<FilterBar>`, status chips, money/date formatting, responsive, loading/empty/error, feedback, accessibility, i18n, gateway banner component): [billing-ui-standards.md](billing-ui-standards.md).

## Gateway banner (all tabs)
When the payment gateway is not configured, a banner shows above the tabs: **"Online payments are not available yet. You can still view invoices and update billing details."** Pay and Add payment method are disabled everywhere on the screen. i18n: `billing.gateway.notConfigured`.

## Tab 1 — Overview
A row of summary cards:

| Card | Content |
|---|---|
| Amount due | Total of OPEN invoices, in their currency (one figure per currency if more than one); shows a **Pay now** button when the total is > 0 and the gateway is configured |
| Next invoice | Date and plan of the next renewal, or "No upcoming invoice" |
| Spent this period | Total paid, from paid invoices (REQ-BIL-001.16), with the change versus last period (for example "+12% vs last period") |
| Default payment method | Card-style tile: type, network or UPI app, last 4, expiry; "None" with an **Add payment method** button |

Below the cards:
- **Alerts strip** — one alert per condition that applies: an invoice past its due date ("Not available until FRD Open question 4 is answered" — due dates are not yet a real rule), and the default card expiring within the next two months ("Your default card ending •••• 1234 expires {month}/{year}. Add a new payment method before it expires.").
- **Spending chart** — bar chart of total paid per period, last 6 periods, computed from paid invoices only (see the `dataviz` skill for chart styling).
- **Recent invoices** — last 5, same columns as the Invoices tab, with **View all**.

## Tab 2 — Invoices
`<DataTable>` with a sticky header and `<FilterBar>` (status chips, issue-date range).

| Column | Type | Notes |
|---|---|---|
| Invoice number | Text | Link to invoice detail; copy-to-clipboard button on hover |
| Issue date | Date | Platform date format |
| Due date | Date | Rule Not specified (FRD Open question 4) |
| Billing period | Date range | |
| Subscription / plan | Text | |
| Subtotal | Money | |
| Tax | Money | Calculated by [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md); shows the tax name on hover (for example "GST 18%") |
| Total | Money | |
| Status | Chip | OPEN, PAID, PARTIALLY_REFUNDED, REFUNDED, VOID — see [status chip mapping](billing-ui-standards.md#status-chips) |
| Actions | Buttons, shown on row hover | **View**, **Pay** (OPEN only; opens [Pay invoice](billing-pay-invoice.md)), **Download invoice**, **Download receipt** (PAID only) |

Sort: issue date, newest first. Pagination: 20 per page.

**Invoice detail** (drawer, laid out like the printed invoice): header (invoice number, status, issue and due dates), bill-to (from Billing details at the time of issue), lines (description, period, quantity, unit price, amount), subtotal, **one line per tax** (tax name, rate, amount — [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md)), total, payments made against it (date, method, amount, status), **Download invoice** / **Download receipt** buttons.

## Tab 3 — Payment methods
Card-style tiles, one per saved method, laid out in a grid: network name and a generic card icon (no third-party logos unless licensed), "•••• 1234", expiry, a default star badge, and an **Expired** ribbon on cards past expiry. Each tile has an overflow (⋮) menu:

| Menu item | Notes |
|---|---|
| Set as default | Not shown on expired cards |
| Remove | Confirmation dialog: "Remove this payment method ending •••• 1234? It will also be deleted from Razorpay." |

An **Add payment method** tile (dashed border, plus icon) sits at the end of the grid → [Add payment method](billing-add-payment-method.md). No card-number fields exist on this screen ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## Tab 4 — Payment history
List grouped by month (most recent month first). Each row: status icon + text, amount, method (type + last 4), invoice number (link); a FAILED row shows its failure reason inline, directly under the row. Filters: status, date range. Razorpay payment ID has a copy button.

## Tab 5 — Billing details
Two-column form on desktop, single column on mobile.

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Billing name (person or legal entity) | Text | Yes | Non-blank, max 200 | `billing.details.name` |
| Billing email (invoices are sent here) | Email | Yes | Valid email | `billing.details.email` |
| Address line 1 | Text | Yes | Non-blank, max 200 | `billing.details.address1` |
| Address line 2 | Text | No | Max 200 | `billing.details.address2` |
| City | Text | Yes | Non-blank | `billing.details.city` |
| State / region | Text | Yes | Non-blank | `billing.details.state` |
| Postal code | Text | Yes | Non-blank; format by country Not specified | `billing.details.postalCode` |
| Country | Select | Yes | From a country list | `billing.details.country` |
| Tax ID (for example GSTIN) | Text | No | Format validation Not specified — see [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) Open question 5 | `billing.details.taxId` |

Inline validation as each field is left (blur). A **"you have unsaved changes"** warning (browser `beforeunload` + in-app route-change confirmation) appears when leaving the tab with unsaved edits. A live preview panel shows how the bill-to block will appear on invoices, updating as the form changes. Buttons: **Save** (disabled until changed), **Cancel**. Changes apply to future invoices only.

## States
| State | Behaviour |
|---|---|
| Loading | Skeletons matching each tab's final layout (summary cards, table rows, tiles) |
| Empty | Invoices: "No invoices yet." Payment methods: "No saved payment methods." Payment history: "No payments yet." |
| Error | Backend message shown as-is with **Retry** |

## Accessibility
Tabs keyboard-navigable; every action has an accessible name; axe test required (FRD AC-18). Details: [billing-ui-standards.md](billing-ui-standards.md#accessibility).
