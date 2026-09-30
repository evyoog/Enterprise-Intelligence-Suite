# UI requirements — Billing & Payments (REQ-BIL-001)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index.

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Billing (tabs: Overview, Invoices, Payment methods, Payment history, Billing details) | `/billing` | Individual customer; organization billing user (Open question 5) | [billing.md](../../../05-ui/screen-requirements/billing.md) |
| Add payment method (dialog) | opened from `/billing` | Same as Billing | [billing-add-payment-method.md](../../../05-ui/screen-requirements/billing-add-payment-method.md) |
| Pay invoice (dialog and result states) | opened from `/billing` | Same as Billing | [billing-pay-invoice.md](../../../05-ui/screen-requirements/billing-pay-invoice.md) |
| Billing admin (tabs: Invoices, Payments) | `/admin/billing` | `MANAGE_BILLING` | [admin-billing.md](../../../05-ui/screen-requirements/admin-billing.md) |
| Payment gateway | `/admin/billing/payment-gateway` | `MANAGE_BILLING` | [admin-payment-gateway.md](../../../05-ui/screen-requirements/admin-payment-gateway.md) |

Screens follow [application-layout.md](../../../05-ui/screen-requirements/application-layout.md) (signed-in tool frame, permission-gated sidebar) and the C44 rule of grouping related functions into one screen with tabs instead of one screen per function.

Wireframes: Not specified.
