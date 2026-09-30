# Screen: Billing

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/billing` (signed-in tool frame, [application-layout.md](application-layout.md)) |
| Sidebar | Workspace → **Billing**, shown to individual customers and to organization billing users (permission: FRD Open question 5) |
| Permissions | Own billing only (individual) or the organization's billing (organization billing user). Others: route hidden; API returns 404 |
| Layout | One screen with five tabs, following the C44 rule of grouping related functions instead of one screen per function |

## Gateway banner (all tabs)
When the payment gateway is not configured, a banner shows above the tabs: **"Online payments are not available yet. You can still view invoices and update billing details."** Pay and Add payment method are disabled everywhere on the screen. i18n: `billing.gateway.notConfigured`.

## Tab 1 — Overview
| Element | Content |
|---|---|
| Amount due | Total of OPEN invoices, in their currency (one figure per currency if more than one) |
| Next invoice | Date and plan of the next renewal, or "No upcoming invoice" |
| Spent this period / last period | Total paid, from paid invoices (REQ-BIL-001.16) |
| Default payment method | Type, network or UPI app, last 4, expiry; "None" with an **Add payment method** button |
| Recent invoices | Last 5, same columns as the Invoices tab, with **View all** |

## Tab 2 — Invoices
| Column | Type | Notes |
|---|---|---|
| Invoice number | Text | Link to invoice detail |
| Issue date | Date | Platform date format |
| Due date | Date | Rule Not specified (FRD Open question 4) |
| Billing period | Date range | |
| Subscription / plan | Text | |
| Subtotal | Money | |
| Tax | Money | Rule Not specified (FRD Open question 1) |
| Total | Money | |
| Status | Chip | OPEN, PAID, PARTIALLY_REFUNDED, REFUNDED, VOID |
| Actions | Buttons | **Pay** (OPEN only; opens [Pay invoice](billing-pay-invoice.md)), **Download invoice**, **Download receipt** (PAID only) |

Filters: status (multi-select), issue-date range. Sort: issue date, newest first. Pagination: 20 per page.

**Invoice detail** (side panel): header (invoice number, status, issue and due dates), bill-to (from Billing details at the time of issue), lines (description, period, quantity, unit price, amount), subtotal, tax, total, payments made against it (date, method, amount, status).

## Tab 3 — Payment methods
| Column | Notes |
|---|---|
| Type | Card or UPI (other types: FRD Open question 7) |
| Details | Card: network, "•••• 1234", expiry MM/YY, card type (credit/debit). UPI: UPI ID as returned (masked) |
| Status | Active, or **Expired** (cards past expiry) |
| Default | Badge on the default method |
| Actions | **Set as default** (not for expired cards), **Remove** (confirmation dialog: "Remove this payment method? It will also be deleted from Razorpay.") |

Button: **Add payment method** → [Add payment method](billing-add-payment-method.md). No card-number fields exist on this screen ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

## Tab 4 — Payment history
Columns: date and time, invoice number (link), amount, currency, method (type + last 4), status (CAPTURED, FAILED, PARTIALLY_REFUNDED, REFUNDED), Razorpay payment ID (copy button), failure reason (FAILED only). Filters: status, date range.

## Tab 5 — Billing details
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
| Tax ID (for example GSTIN) | Text | No | Format validation Not specified (FRD Open question 1) | `billing.details.taxId` |

Buttons: **Save** (disabled until changed), **Cancel**. Changes apply to future invoices only.

## States
| State | Behaviour |
|---|---|
| Loading | Skeleton rows per tab |
| Empty | Invoices: "No invoices yet." Payment methods: "No saved payment methods." Payment history: "No payments yet." |
| Error | Backend message shown as-is with **Retry** |

## Accessibility
Tabs keyboard-navigable; every action has an accessible name; axe test required (FRD AC-18).
