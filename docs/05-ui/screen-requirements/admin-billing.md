# Screen: Billing admin

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.9, .12](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing` |
| Sidebar | Admin → **Billing** (children: Invoices & payments, Payment gateway) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Layout | One screen, two tabs |

## Tab 1 — Invoices
Columns: invoice number, customer or organization, issue date, due date, total, currency, status. Filters: status, customer/organization search, issue-date range. Row click opens the same invoice detail as the customer screen, plus the customer's identity.

## Tab 2 — Payments
Columns: date and time, customer or organization, invoice number, amount, refunded amount, currency, method (type + last 4), status, Razorpay payment ID. Filters: status, date range, search by invoice number or payment ID.

### Payment detail (side panel)
Everything in the row, plus: failure reason, refunds (date, amount, reason, status, admin), webhook events received for this payment (type, time).

Actions:
- **Refund** (CAPTURED or PARTIALLY_REFUNDED; disabled when the gateway is not configured) opens a dialog:

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Amount | Money | Yes | > 0 and ≤ captured minus already refunded ([business rules](../../02-requirements/FRD/billing-payments/business-rules.md) BR-7) | `admin.billing.refund.amount` |
| Reason | Text | Yes | Non-blank, max 500 | `admin.billing.refund.reason` |

  Buttons: **Refund {amount}** (confirmation: "Refund {amount} to the customer? This cannot be undone."), **Cancel**.
- **Reconcile** fetches the payment's current status from Razorpay and shows what changed.

## States
Loading skeleton; empty ("No invoices yet." / "No payments yet."); backend errors shown as-is with **Retry**; gateway-not-configured banner as on the customer screen.
