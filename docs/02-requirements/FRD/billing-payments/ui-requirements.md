# UI requirements — Billing & Payments (REQ-BIL-001)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index.

Shared presentation standards (theme, `<DataTable>`/`<FilterBar>`, status chips, money/date formatting, responsive, states, feedback, accessibility, i18n, gateway banner): [billing-ui-standards.md](../../../05-ui/screen-requirements/billing-ui-standards.md).

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Billing (tabs: Overview, Invoices, Payment methods, Payment history, Billing details) | `/billing` | Individual customer; organization member with Manage billing (BI-5; others see an explanation) | [billing.md](../../../05-ui/screen-requirements/billing.md) |
| Add payment method (dialog) | opened from `/billing` | Same as Billing | [billing-add-payment-method.md](../../../05-ui/screen-requirements/billing-add-payment-method.md) |
| Pay invoice (dialog and result states) — superseded by the checkout (C55) | opened from `/billing` | Same as Billing | [billing-pay-invoice.md](../../../05-ui/screen-requirements/billing-pay-invoice.md) |
| Billing admin (tabs: Invoices, Payments) | `/admin/billing` | `MANAGE_BILLING` | [admin-billing.md](../../../05-ui/screen-requirements/admin-billing.md) |
| Payment gateway | `/admin/billing/payment-gateway` | `MANAGE_BILLING` | [admin-payment-gateway.md](../../../05-ui/screen-requirements/admin-payment-gateway.md) |
| Checkout payment (steps: Cart › Billing details › Payment › Complete; C59 redesign) — REQ-BIL-001.18/.19/.22 | `/checkout?invoiceId=…` (or `?subscriptionId=…`) | Owner of the subscription/invoice; organization billing user (Open question 5) | [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md) |
| Billing settings (business profile and policy links, offline bank details, payment methods and the organizations allowed to pay by invoice, **Payment gateways**, renewal reminders) — REQ-BIL-001.21, .24, .28, .30, .31 | `/admin/billing/settings` | `MANAGE_BILLING` | [admin-billing-settings.md](../../../05-ui/screen-requirements/admin-billing-settings.md) |
| Payment brand assets (logos, icons) — REQ-BIL-001.23 | — | — | [payment-brand-assets.md](../../../05-ui/screen-requirements/payment-brand-assets.md) |

Screens for the related FRDs built alongside this one:

| Screen | FRD | Screen requirement |
|---|---|---|
| Tax rules admin | [REQ-BIL-002](../../../02-requirements/FRD/tax-rules/requirement.md) | [admin-tax-rules.md](../../../05-ui/screen-requirements/admin-tax-rules.md) |
| Entitlements (section on My subscriptions, admin lookup) | [REQ-SUB-002](../../../02-requirements/FRD/entitlements/requirement.md) | [entitlements.md](../../../05-ui/screen-requirements/entitlements.md) |
| My subscriptions (scheduled downgrade) | [REQ-SUB-001](../../../02-requirements/FRD/subscription-lifecycle/requirement.md) | [my-subscriptions.md](../../../05-ui/screen-requirements/my-subscriptions.md) |
| Cart | [REQ-MKT-003](../../../02-requirements/FRD/cart-checkout/requirement.md) | [cart.md](../../../05-ui/screen-requirements/cart.md) |

Screens follow [application-layout.md](../../../05-ui/screen-requirements/application-layout.md) (signed-in tool frame, permission-gated sidebar) and the C44 rule of grouping related functions into one screen with tabs instead of one screen per function.

## Changes from the sprint 2026.4.3 answers ([C97](../../../01-business/roadmap/open-decisions.md#c97))
- **Payment screens are gateway-neutral** (BI-8): no screen names a gateway except where a gateway's own credentials are shown (the Razorpay credentials panel) and the "Powered by" badge, which shows the Razorpay logo for Razorpay and the gateway's name for any other.
- **Checkout** ([checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md)): the consent sentence links to the Terms, Refund and Privacy pages (BI-7); an optional purchase-order field (BI-9); Pay by invoice only for enabled organizations (BI-4).
- **Billing** ([billing.md](../../../05-ui/screen-requirements/billing.md)): a no-permission state for members without Manage billing (BI-5).
- **Billing settings** ([admin-billing-settings.md](../../../05-ui/screen-requirements/admin-billing-settings.md)): policy links, organizations allowed to pay by invoice, Payment gateways panel.

Wireframes: Not specified.
