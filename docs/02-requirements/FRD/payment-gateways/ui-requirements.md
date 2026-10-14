# UI requirements — Payment gateways (REQ-BIL-003)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index and the summary of what the screens must do.

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Payment Gateways (overview, gateway list, planned providers, Configure dialog, Details dialog with Overview / Webhook / Activity) | `/admin/billing/payment-gateway` | Platform permission `MANAGE_BILLING` | [admin-payment-gateway.md](../../../05-ui/screen-requirements/admin-payment-gateway.md) |
| Billing settings, tab **Payment gateways** (embeds the same list, dialogs and overview) | `/admin/billing/settings?tab=gateway` | `MANAGE_BILLING` | [admin-billing-settings.md](../../../05-ui/screen-requirements/admin-billing-settings.md#tab-4--payment-gateways) |
| Checkout payment (shows the provider the backend chose; an unsupported provider shows an error and nothing is charged) | `/checkout?invoiceId=…` | Owner of the invoice; organization billing user | [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md#gateway-chosen-by-the-backend-c98) |

## Rules every gateway screen follows
- **Everything shown comes from the backend** (the registry and the saved configuration): provider names, logo keys, methods, currencies, fields, set-up steps, webhook endpoint. No provider is hard-coded in the list, the configure form or the details (BR-2).
- **Only the actions valid for the state** are shown: a planned provider has none (only View details); Configure for an implemented one; Test connection once configured; Activate when configured and inactive; Deactivate when active; Make default when active and not default.
- **Four separate status chips** (Configured, Connected, Active, Default) and never one merged label (BR-6).
- **Secrets are write-only**: password-type inputs that start empty, a saved secret shows only "Set (stored here)" or "Set (secrets file)", a blank keeps it (BR-4).
- **Honest outcomes**: the real result of a test or a refused action is shown in the row, with the server's message.
- WCAG 2.1 AA, every string through i18n under `admin.paymentGateway`, light and dark themes, phone width (see the screen file).

Wireframes: Not specified.
