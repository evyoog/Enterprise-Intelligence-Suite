# Screen: Payment Gateways

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-003](../../02-requirements/FRD/payment-gateways/requirement.md) ([C98](../../01-business/roadmap/open-decisions.md#c98)); replaces the read-only Razorpay view of [REQ-BIL-001.13, .14](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing/payment-gateway` (sidebar: Admin → Billing → **Payment gateways**, `labelKey` `paymentGateway`) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Built | 2026-10-14. `frontend/src/pages/admin/AdminPaymentGatewayPage.tsx` (header) and `frontend/src/components/payments/PaymentGatewayManagement.tsx` (the list, shared with Billing settings), `GatewayConfigureDialog.tsx`, `GatewayDetailsDialog.tsx`, `GatewayLogo.tsx`, `gatewayStatus.ts`; API client `frontend/src/api/paymentGatewayApi.ts` |
| Rules | [business-rules.md](../../02-requirements/FRD/payment-gateways/business-rules.md). **Credentials can be entered here** (a change to [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md) rule 5 for this feature, [C98](../../01-business/roadmap/open-decisions.md#c98)); they are write-only and never displayed |
| API | [payment-gateways.md](../../06-api/api-requirements/payment-gateways.md) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Colours come from the unified MUI theme (light and dark). The same list, dialogs and overview are embedded in [Billing settings → Payment gateways](admin-billing-settings.md#tab-4--payment-gateways); this page is the same content with its own header.

**Nothing on this screen is hard-coded for a provider.** Names, logo keys, methods, currencies, configuration fields, set-up steps and webhook events come from the backend registry ([BR-2](../../02-requirements/FRD/payment-gateways/business-rules.md)). A provider added to the backend appears here with no UI change.

## Page header
Plug icon (amber), eyebrow "Billing", title **Payment Gateways** (`admin.paymentGateway.title`), subtitle "Configure and manage payment providers used to collect subscription payments across EIS." (`admin.paymentGateway.subtitle`). Action on the right: an outlined button **Payment methods** to `/admin/billing/settings?tab=methods`.

## Overview statistics
Four tiles in a description list (`dl`; two per row on a phone, four on desktop), from `GET /admin/billing/gateways/summary`:

| Tile | Meaning | i18n key |
|---|---|---|
| Registered gateways | Every provider in the registry, planned ones included | `admin.paymentGateway.stats.registered` |
| Configured | Implemented providers whose required fields are all set | `…stats.configured` |
| Active | Gateways accepting new payments | `…stats.active` |
| Need configuration | Implemented providers that are not fully configured | `…stats.needsConfiguration` |

## Payment gateways list
Section heading **Payment gateways** (`…configuredTitle`), one **card per implemented provider** (a list item whose accessible name is the provider name), in the order the backend returns. Under the list, the routing note (`…routingNote`): new payments go to an active gateway that supports the invoice currency and method, the default first; payments already made stay with the gateway that took them.

Each card:

| Element | Content |
|---|---|
| Logo and name | The provider logo when EIS holds the official file for its logo key, otherwise a neutral two-letter monogram ([payment-brand-assets.md](payment-brand-assets.md)). Name as heading level 3. Below it the **environment** (Test or Live) or "Environment not set" |
| Methods and currencies | The enabled payment methods (labels `…methods.card`, `upi`, `netbanking`, `wallet`, `bank_transfer`, `paypal`; an unknown method shows its code) and the currency codes |
| Status chips | **Configuration** chip: Not configured / Configuration incomplete / Configured. **Connection** chip (outlined; only when Configured): Not tested, Connection successful, Authentication failed, Provider unavailable, Unsupported configuration, Test failed (the message is its tooltip). **Active** (filled) or **Inactive** (outlined). **Default** chip on the default gateway. The four are separate and never merged ([BR-6](../../02-requirements/FRD/payment-gateways/business-rules.md)). Colours: green success, amber warning, red error, grey neutral (`gatewayStatus.ts`) |
| Last test | "Last test: {date and time}" and the provider's message, when the tested configuration is the current one |
| Row notice | Result of the last action on this card: green for success, red with the server's message for a refusal (`role="status"`, dismissible) |

### Actions per row (only those valid for the state)
The backend sends `actions` for each row; the screen shows a button only when its flag is true.

| Button | Shown when | What it does | Accessible name |
|---|---|---|---|
| **Configure** | Implemented | Opens the Configure dialog | "Configure {name}" |
| **Test connection** | Configured | Real test, shows the outcome and the provider's message in the row notice and refreshes the list | "Test the {name} connection" |
| **Activate** | Configured and not active | Server tests again and activates only if it passes; success "{name} is active."; a refusal shows the server's message | "Activate {name}" |
| **Make default** | Active and not default | Makes it the default: "{name} is now the default for new payments." | "Make {name} the default gateway" |
| **Deactivate** | Active | "{name} is inactive. Payments already made with it are unchanged." | "Deactivate {name}" |
| **View details** | Always | Opens the Details dialog | "View details of {name}" |

While an action runs the card's buttons are disabled and a small progress indicator ("Working") shows. After every action the list and the overview reload from the backend.

## Planned providers
Shown only when the registry has planned providers (today Stripe, PayPal, PayU and Cashfree). Section heading **Planned integrations** with the text "These providers can be added to EIS but have no integration yet. They cannot be configured, tested or activated."

One compact card per planned provider: logo, name, supported methods, an outlined chip **Integration planned**, and one button, **View details**. **No Configure, Test, Activate or Default button, and no Active or Default chip, ever** ([BR-1](../../02-requirements/FRD/payment-gateways/business-rules.md)). Its details dialog shows the info alert "{name} is not integrated yet. This shows what it will need; nothing here can be configured or used." and only the Overview tab, with the field list marked "Secret" or "Identifier" instead of set / not set.

## Configure dialog
Opened by **Configure**; title "Configure {name}"; loads `GET /admin/billing/gateways/{code}`. **The form is generated from the provider's field list.**

| Element | Behaviour |
|---|---|
| Warning (when `encryptionAvailable` is false) | "Credentials cannot be stored because the server has no encryption key (PAYMENT_GATEWAY_ENCRYPTION_KEY). Ask whoever runs the server to set it. Credentials supplied through the secrets file keep working." Save stays disabled |
| Error (when `credentialsUnreadable`) | "The stored credentials cannot be read with the server's current encryption key. Enter them again." |
| Help | "Enter the credentials from the provider's dashboard. They are stored encrypted and are never shown again; leave a field blank to keep what is saved." |
| Info (when the gateway is active) | "This gateway is active, so the new credentials are tested first. If the test fails nothing is saved and the current credentials keep working." |
| **Environment** | A select with the provider's environments (Test, Live); starts at the saved one, else the first |
| One input per field | Label from the registry; `type="password"` for a secret field, text otherwise; `autocomplete="off"`, no spell check, max 500 characters; **always starts empty**. Required marker only when not already set. Placeholder: for a saved secret "•••••• saved — leave blank to keep"; for a saved identifier its masked form. Helper text: "Set (stored here)" / "Set (secrets file)" / "Not set", then "Current: {masked}" for an identifier, then the provider's help text |
| **Save** | Enabled only when something changed (a typed value or a different environment) **and** the server can store credentials. Sends the environment and **only the fields that were typed**. A refusal (400 or 409, including "Not saved: … The previous configuration is unchanged.") shows inside the dialog and nothing closes. Success closes it with the toast "{name} configuration saved." and reloads the list |
| **Cancel** | Closes; disabled while saving |

## Details dialog
Opened by **View details**; title is the provider logo and name; tabs (accessible name "Gateway details"): **Overview**, **Webhook**, **Activity** (a planned provider has Overview only).

**Overview:** the description; "Countries and currencies"; "Supports" chips (environments, Webhooks, Refunds, Recurring payments); **Payment methods**: one switch per supported method (planned providers: plain chips), the last method still on cannot be switched off, with the help "Switch off a method to stop offering it through this gateway. At least one must stay on."; **Configuration fields** with Set (source) / Not set per field; **Verified by real activity**: four lines, *Credentials accepted*, *Signed webhook received*, *Payment captured*, *Refund made*, each "Yes" (check icon) or "Not yet", with the note that a passing test does not prove payments, webhooks or refunds work ([BR-16](../../02-requirements/FRD/payment-gateways/business-rules.md)).

**Webhook:** the warning "This URL points at this machine, so the provider cannot reach it. Set the public backend address (BACKEND_URL) for this environment." when `reachableFromInternet` is false; the **Webhook URL** in a read-only box with a **Copy** button ("Copy the webhook URL", tooltip Copy / Copied); chips: Webhook secret set / missing, Signature verified / No signed event yet / No webhooks, and "{n} rejected webhook(s)" when there are any; "Last event received: {type · date}" or Never; a warning with the last rejected webhook's date and reason; "Subscribe the webhook to" with the event names; **Set-up steps** as a numbered list from the provider.

**Activity:** "Recent webhook events" (up to 8: date, event type, payment number) or "No webhook has been received."; "Changes by administrators" (the last 10 audit entries: date, text, actor) or "No changes recorded yet."; "Configuration version {n}" and when the credentials were last updated. **Close** button.

## States
| State | Behaviour |
|---|---|
| Loading | A skeleton card block; each dialog shows a skeleton until its data loads |
| Load error | The server's message (or "Could not load the payment gateways.") in an error alert with **Retry** |
| Empty | The registry always has at least one provider; the planned section is hidden when there are none |
| Action error | In the card's row notice (list actions) or in the dialog (configure, methods) |
| No encryption key | Configure dialog warning, Save disabled; credentials from the secrets file keep working |
| Local webhook URL | Warning on the Webhook tab |

## Accessibility
WCAG 2.1 AA; automated axe check on the page (`AdminPaymentGatewayPage.test.tsx`). Each card is a list item named by the provider; every action button has an accessible name that includes the provider name; the overview is a description list; status chips carry text (never colour alone); dialogs are labelled by their titles and trap focus; the tab set has an accessible name; row notices are announced (`role="status"`) and the working indicator has the accessible name "Working"; secret inputs are password fields that are never prefilled. Keyboard: every control is reachable and has a visible focus indicator.

## i18n
Every string is under `admin.paymentGateway` in `frontend/src/i18n/locales/en.json` and `es.json` (the sidebar label is `appShell.nav.paymentGateway`). Groups: `title`, `subtitle`, `stats.*`, `configuration.*` (status labels), `connection.*` (the eight connection statuses), `env.*`, `methods.*`, `source.*` (STORED, ENVIRONMENT, NONE), `tabs.*`, `cap.*`, `evidenceItems.*`, `signature.*`; and the single keys for the buttons and their accessible names (`configure`, `configureNamed`, `testConnection`, `testNamed`, `activate`, `activateNamed`, `deactivate`, `deactivateNamed`, `makeDefault`, `makeDefaultNamed`, `viewDetails`, `viewDetailsNamed`), the notices (`activated`, `deactivated`, `defaultSet`, `saved`, `saveFailed`, `testFailed`, `loadError`), and the dialog texts (`configureHelp`, `rotationNote`, `noEncryptionKey`, `unreadable`, `keepSecret`, `fieldSet`, `fieldNotSet`, `current`, `localUrl`, `plannedNote`, and so on). Provider names, descriptions, field labels and set-up steps come from the backend and are not translated by the screen. A method code without a `methods.*` key shows the code.

## Known discrepancy
The Webhook tab's local-address warning names the variable `BACKEND_URL`, which the backend reads as the property `app.backend-url` (`backend/src/main/resources/application.yml`). Operators must set `BACKEND_URL` to the public backend address.

## Not on this screen
Switching Razorpay keys by editing `config/secrets.env` still works (BR-5) but is not described here: see [09-integrations/razorpay.md](../../09-integrations/razorpay.md). Routing by country or amount, fail-over and recurring charges are Not specified ([C98](../../01-business/roadmap/open-decisions.md#c98)).

## API used
`GET /admin/billing/gateways`, `GET /admin/billing/gateways/summary`, `GET /admin/billing/gateways/{code}`, `PUT …/{code}/configuration`, `POST …/{code}/test`, `POST …/{code}/activate`, `POST …/{code}/deactivate`, `PUT …/{code}/methods`, `PUT /admin/billing/gateways/default` — all in [payment-gateways.md](../../06-api/api-requirements/payment-gateways.md).

## Built
2026-10-14. Tests: `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx`; test cases in [`test-cases/functional/payment-gateways/`](../../../test-cases/functional/payment-gateways/TESTPLAN-BIL-003.md).

> **History.** The C60 version of this screen (a read-only Razorpay panel with the setup checklist and keys in `config/secrets.env`) and the C97 BI-8 gateway list (Billing settings → Payment gateways) are replaced by this screen.
