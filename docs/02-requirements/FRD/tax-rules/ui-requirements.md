# UI requirements — Tax Rules and Calculation (REQ-BIL-002)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index and the summary of the template sections; the screen specification is the source of truth.

## Screens
| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Tax configuration: External Tax Engine switch with active mode and status, then tabs for rules (approval workflow, versions, preview calculator), classification (tax classes and products), exemptions, registrations, external provider (connection test, capability validation) and Draft invoices (Retry tax) | `/admin/billing/tax` (`?tab=rules\|classification\|exemptions\|registrations\|provider\|blocked`) | `MANAGE_BILLING` (platform administrator) | [admin-tax-rules.md](../../../05-ui/screen-requirements/admin-tax-rules.md) |

Related screens that show tax but are specified elsewhere: invoice and credit-note views and documents, checkout summary and billing details ([billing.md](../../../05-ui/screen-requirements/billing.md), [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md), [admin-billing.md](../../../05-ui/screen-requirements/admin-billing.md), REQ-BIL-001). Admin → Billing shows a **Resolve tax** link on a Draft invoice that opens the Draft invoices tab.

Follows [application-layout.md](../../../05-ui/screen-requirements/application-layout.md) and the shared [billing-ui-standards.md](../../../05-ui/screen-requirements/billing-ui-standards.md).

## Fields and validation
Defined per form in the screen requirement: the **External Tax Engine** switch (label, description "Enable or disable external tax calculation for billing transactions.", confirmation), the rule form (country, state, tax type, class, treatment, supply type, customer type, tax ID condition, effective dates, components with rate 0 to 100 and up to 4 decimals), the tax class form, the exemption form, the registration form, the provider settings (timeout 2 to 60 s, attempts 1 to 5) and the preview calculator. Rules enforced by the server are in [business-rules.md](business-rules.md) (BR-16, BR-22 to BR-24, BR-30); the client repeats only the format checks and shows the server's own message when it refuses.

## States
- Empty: each tab has its own empty message (no rules, no classes, no exemptions, no registrations, no invoices waiting for tax, provider not validated yet).
- Loading: skeleton placeholders under the header and in each tab.
- Error: the server's message, or "Tax configuration could not be loaded.", with **Try again**.
- Blocked: status "Blocking invoices" on the engine card, a count badge on the Draft invoices tab, and each Draft listed with its block code and reason.
- Refused: switching External on without a passing provider keeps the switch Off and lists every failed check.

## Accessibility and localization
- Named regions, a labelled tab bar with tab panels, switch with accessible name and text state, status by icon and text, labelled dialogs and fields, `role="alert"` on errors; the page passes the axe check.
- All text is under the `admin.tax` key of `frontend/src/i18n/locales/` (English and Spanish); dates follow the active language; nothing is hard-coded, including rates.
