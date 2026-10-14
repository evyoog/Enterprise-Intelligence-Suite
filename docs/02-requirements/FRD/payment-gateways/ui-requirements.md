# UI requirements — Payment gateways (REQ-BIL-003)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index and the summary of what the screens must do.

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Payment Gateways (overview, gateway list, **Add payment gateway** and Edit dialog, Remove confirmation, Configure dialog, Details dialog with Overview / Webhook / Activity) | `/admin/billing/payment-gateway` | Platform permission `MANAGE_BILLING` | [admin-payment-gateway.md](../../../05-ui/screen-requirements/admin-payment-gateway.md) |
| Billing settings, tab **Payment gateways** (embeds the same list, dialogs and overview) | `/admin/billing/settings?tab=gateway` | `MANAGE_BILLING` | [admin-billing-settings.md](../../../05-ui/screen-requirements/admin-billing-settings.md#tab-4--payment-gateways) |
| Checkout payment (shows the provider the backend chose; an unsupported provider shows an error and nothing is charged) | `/checkout?invoiceId=…` | Owner of the invoice; organization billing user | [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md#gateway-chosen-by-the-backend-c98) |

## Rules every gateway screen follows
- **Everything shown comes from the backend** (the registry and the saved configuration): provider names, logo keys, methods, currencies, fields, set-up steps, webhook endpoint. No provider is hard-coded in the list, the configure form or the details (BR-2). Template names (Stripe, PayPal, PayU, Cashfree) also come from the backend (`GET /admin/billing/gateways/templates`, BR-25).
- **Only the actions valid for the state** are shown: a gateway added by an administrator (no payment integration, BR-23) has only Configure, View details, **Edit** and **Remove**; a built-in provider has Configure, Test connection once configured, Activate when configured and inactive, Deactivate when active and Make default when active and not default. There is no "planned" provider and no Planned integrations section ([C99](../../../01-business/roadmap/open-decisions.md#c99)).
- **A gateway without an integration is labelled as such:** the chip "No payment integration" and the line "Added here: the credentials are stored encrypted, but EIS has no payment integration for this gateway, so it cannot be tested, activated or used to take payments." appear on its row, and the same note on its details; it never shows a connection result, a Verified signature or the evidence flags (BR-23).
- **Add payment gateway** is a button above the list; its dialog states that values are stored encrypted and never shown again and that a gateway added here has no payment integration (BR-20 to BR-22); credential values are password inputs for secret fields and are never shown again.
- **Four separate status chips** (Configured, Connected, Active, Default) and never one merged label (BR-6).
- **Secrets are write-only**: password-type inputs that start empty (in the Add dialog the optional value inputs of secret fields are passwords too), a saved secret shows only "Set (stored here)" or "Set (secrets file)", a blank keeps it (BR-4).
- **Honest outcomes**: the real result of a test or a refused action is shown in the row, with the server's message.
- WCAG 2.1 AA, every string through i18n under `admin.paymentGateway`, light and dark themes, phone width (see the screen file).

Wireframes: Not specified.
