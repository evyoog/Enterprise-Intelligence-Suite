# Screen: Billing admin

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.9, .12](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing` |
| Sidebar | Admin → **Billing** (children: Invoices & payments, Payment gateway, [Tax](admin-tax-rules.md)) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Layout | One screen, two tabs |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## Summary strip (both tabs)
Four cards above the tabs: collected this period, refunds this period, failed payments this period, open invoice total.

## Tab 1 — Invoices
`<DataTable>` + `<FilterBar>` (status, customer/organization search, issue-date range) — the active filter is kept in the URL so a filtered view can be shared by link. Columns: invoice number, customer or organization, issue date, due date, total, currency, status. Row click opens the same invoice detail as the customer screen, plus the customer's identity.

## Tab 2 — Payments
`<DataTable>` + `<FilterBar>` (status, date range, search by invoice number or payment ID) — filter state kept in the URL. Columns: date and time, customer or organization, invoice number, amount, refunded amount, currency, method (type + last 4), status, Razorpay payment ID.

### Payment detail (drawer)
Everything in the row, plus an **event timeline**: created → captured / failed → refunds → webhook events, each with its timestamp.

Actions:
- **Refund** (CAPTURED or PARTIALLY_REFUNDED; disabled when the gateway is not configured) opens a dialog:

| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Amount | Money | Yes | > 0 and ≤ captured minus already refunded ([business rules](../../02-requirements/FRD/billing-payments/business-rules.md) BR-7) | `admin.billing.refund.amount` |
| Reason | Text | Yes | Non-blank, max 500 | `admin.billing.refund.reason` |

  The dialog shows "Refundable: {refundable} of {captured}" and a live preview of the resulting payment status (CAPTURED → PARTIALLY_REFUNDED or REFUNDED) as the amount is typed. Buttons: **Refund {amount}** (confirmation: "Refund {amount} to the customer? This cannot be undone."), **Cancel**.
- **Reconcile** fetches the payment's current status from Razorpay and shows what changed.

## States
Loading skeletons matching the table/card layout; empty ("No invoices yet." / "No payments yet."); backend errors shown as-is with **Retry**; gateway-not-configured banner as on the customer screen.
