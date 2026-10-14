# Screen: Payment Gateways

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-003](../../02-requirements/FRD/payment-gateways/requirement.md) ([C98](../../01-business/roadmap/open-decisions.md#c98); administrator-added gateways [C99](../../01-business/roadmap/open-decisions.md#c99), REQ-BIL-003.16 and .17); replaces the read-only Razorpay view of [REQ-BIL-001.13, .14](../../02-requirements/FRD/billing-payments/requirement.md) |
| Route | `/admin/billing/payment-gateway` (sidebar: Admin → Billing → **Payment gateways**, `labelKey` `paymentGateway`) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |
| Built | 2026-10-14. `frontend/src/pages/admin/AdminPaymentGatewayPage.tsx` (header) and `frontend/src/components/payments/PaymentGatewayManagement.tsx` (the list, shared with Billing settings), `GatewayConfigureDialog.tsx`, `GatewayDetailsDialog.tsx`, `GatewayFormDialog.tsx` (Add and Edit, C99), `GatewayLogo.tsx`, `gatewayStatus.ts`; API client `frontend/src/api/paymentGatewayApi.ts` |
| Rules | [business-rules.md](../../02-requirements/FRD/payment-gateways/business-rules.md). **Credentials can be entered here** (a change to [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md) rule 5 for this feature, [C98](../../01-business/roadmap/open-decisions.md#c98)); they are write-only and never displayed. **A gateway added here is configuration only** (no payment integration, [BR-23](../../02-requirements/FRD/payment-gateways/business-rules.md)) |
| API | [payment-gateways.md](../../06-api/api-requirements/payment-gateways.md) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Colours come from the unified MUI theme (light and dark). The same list, dialogs and overview are embedded in [Billing settings → Payment gateways](admin-billing-settings.md#tab-4--payment-gateways); this page is the same content with its own header.

**Nothing on this screen is hard-coded for a provider.** Names, logo keys, methods, currencies, configuration fields, set-up steps and webhook events come from the backend registry ([BR-2](../../02-requirements/FRD/payment-gateways/business-rules.md)). A provider added to the backend appears here with no UI change, and so does a gateway an administrator adds with the Add dialog. There is **no Planned integrations section** and no provider is listed unless it is a built-in provider or was added ([C99](../../01-business/roadmap/open-decisions.md#c99)); Stripe, PayPal, PayU and Cashfree appear only as templates inside the Add dialog.

## Page header
Plug icon (amber), eyebrow "Billing", title **Payment Gateways** (`admin.paymentGateway.title`), subtitle "Configure and manage payment providers used to collect subscription payments across EIS." (`admin.paymentGateway.subtitle`). Action on the right: an outlined button **Payment methods** to `/admin/billing/settings?tab=methods`.

## Overview statistics
Four tiles in a description list (`dl`; two per row on a phone, four on desktop), from `GET /admin/billing/gateways/summary`:

| Tile | Meaning | i18n key |
|---|---|---|
| Registered gateways | Every gateway listed: built-in providers and gateways an administrator added | `admin.paymentGateway.stats.registered` |
| Configured | Gateways whose required fields are all set (an added gateway counts) | `…stats.configured` |
| Active | Gateways accepting new payments (an added gateway never does) | `…stats.active` |
| Need configuration | Every gateway that is not fully configured, added ones included | `…stats.needsConfiguration` |

## Payment gateways list
Section heading **Payment gateways** (`…configuredTitle`), one **card per gateway** (a list item whose accessible name is the gateway name), in the order the backend returns (built-in providers first, then added gateways by code). Next to the heading, the contained button **Add payment gateway** (`admin.paymentGateway.form.addButton`) opens the [Add dialog](#add-and-edit-payment-gateway-dialog). Under the list, the routing note (`…routingNote`): new payments go to an active gateway that supports the invoice currency and method, the default first; payments already made stay with the gateway that took them.

Each card:

| Element | Content |
|---|---|
| Logo and name | The provider logo when EIS holds the official file for its logo key, otherwise a neutral two-letter monogram ([payment-brand-assets.md](payment-brand-assets.md)). Name as heading level 3. Below it the **environment** (Test or Live) or "Environment not set" |
| Methods and currencies | The enabled payment methods (labels `…methods.card`, `upi`, `netbanking`, `wallet`, `bank_transfer`, `paypal`; an unknown method shows its code) and the currency codes |
| Status chips | **Configuration** chip: Not configured / Configuration incomplete / Configured. **Connection** chip (outlined; only when Configured and the gateway has an integration): Not tested, Connection successful, Authentication failed, Provider unavailable, Unsupported configuration, Test failed (the message is its tooltip). **For a gateway added by an administrator** the Connection chip is replaced by an outlined **No payment integration** chip (its tooltip is "No payment integration exists for this gateway, so it cannot be tested or activated."), and the card shows the line "Added here: the credentials are stored encrypted, but EIS has no payment integration for this gateway, so it cannot be tested, activated or used to take payments." **Active** (filled) or **Inactive** (outlined; an added gateway is always Inactive). **Default** chip on the default gateway (never on an added one). The four are separate and never merged ([BR-6](../../02-requirements/FRD/payment-gateways/business-rules.md)). Colours: green success, amber warning, red error, grey neutral (`gatewayStatus.ts`) |
| Last test | "Last test: {date and time}" and the provider's message, when the tested configuration is the current one |
| Row notice | Result of the last action on this card: green for success, red with the server's message for a refusal (`role="status"`, dismissible) |

### Actions per row (only those valid for the state)
The backend sends `actions` for each row; the screen shows a button only when its flag is true.

| Button | Shown when | What it does | Accessible name |
|---|---|---|---|
| **Configure** | Always | Opens the Configure dialog (works for an added gateway: enter or rotate its credential values, no test is run) | "Configure {name}" |
| **Test connection** | Configured and has an integration (never for an added gateway) | Real test, shows the outcome and the provider's message in the row notice and refreshes the list | "Test the {name} connection" |
| **Activate** | Configured, has an integration and not active | Server tests again and activates only if it passes; success "{name} is active."; a refusal shows the server's message | "Activate {name}" |
| **Make default** | Active and not default (built-in only) | Makes it the default: "{name} is now the default for new payments." | "Make {name} the default gateway" |
| **Deactivate** | Active (built-in only) | "{name} is inactive. Payments already made with it are unchanged." | "Deactivate {name}" |
| **View details** | Always | Opens the Details dialog | "View details of {name}" |
| **Edit** | Only a gateway an administrator added (`actions.edit`) | Opens the [Edit dialog](#add-and-edit-payment-gateway-dialog) | "Edit {name}" |
| **Remove** | Only a gateway an administrator added (`actions.remove`) | Opens the [confirmation](#remove-confirmation) | "Remove {name}" |

While an action runs the card's buttons are disabled and a small progress indicator ("Working") shows. After every action the list and the overview reload from the backend.

## Add and edit payment gateway dialog
Opened by **Add payment gateway** (title "Add payment gateway") or, on an added gateway, **Edit** (title "Edit payment gateway", loads `GET /admin/billing/gateways/{code}`; `GatewayFormDialog.tsx`). A wide dialog (phone: one column). It starts with the info alert "Define the gateway and the credentials it needs. Values you enter are stored encrypted in the database and are never shown again."

| Field | Behaviour |
|---|---|
| **Start from** (Add only; shown when templates exist) | Select: "Other (enter everything yourself)" and one option per template (Stripe, PayPal, PayU, Cashfree, from `GET …/templates`; one already on the page is not offered). Choosing a template fills name, code, description, logo key, currencies, countries, payment methods and the credential rows (values empty); every field can still be changed. Help: "Fills in the usual name and credential fields of a well-known provider. You can change anything." |
| **Gateway name** (required) | Up to 100 characters. While the code has not been typed, the code follows the name (capital letters, digits and underscores, up to 30; "G_" is put in front when it would not start with a letter) |
| **Gateway code** (required) | Up to 30 characters, typed in capitals; help "Letters, digits and underscores. Cannot be changed later."; **disabled when editing** |
| **Description** | Optional, up to 300 characters |
| **Environment** (Add only) | Select Test or Live; starts at Test |
| **Currencies** (required) | Text, three-letter codes separated by commas, spaces or semicolons; starts at "INR"; help "Three-letter codes, comma separated, e.g. INR, USD." |
| **Countries** | Optional, two-letter codes; help "Optional, two-letter codes, e.g. IN, US." |
| **Payment methods** | Check boxes: Cards, UPI, Netbanking, Wallets, Bank transfer, PayPal (labels `…methods.*`); starts with Cards; at least one |
| **Credentials** | One group per credential, accessible name "Credential {n}" (at most 12; **Add credential** button; a trash button "Remove credential {n}" disabled while only one remains). Each has: **Label** (required, up to 60), **Name** (required, up to 40, letters and digits; filled in from the label in camel case until typed; **locked** for an existing credential when editing; help "Letters and digits; filled in from the label."), **Value** (Add only; optional; a password input for a secret credential, text otherwise; up to 500 characters, `autocomplete="off"`; help "Optional now; you can enter it later with Configure."), switches **Secret** (starts on) and **Required** (starts on). Intro text: Add "List what the provider gives you (keys, secrets, IDs). Secret values are hidden when typed and can only be replaced later, never read."; Edit "Add, rename or remove credentials. Values are entered with Configure; removing a credential deletes its stored value." (renaming applies to the label; an existing credential's name is locked) |
| Warning | "EIS has no payment integration for a gateway added here. It stores the configuration and credentials, but cannot be tested, activated or used to take payments until an integration is built for it." Always shown |
| Problem line (`role="status"`) | The first problem the form can see, while any exists; **Add gateway** (Edit: **Save**) stays disabled until there is none |
| Error alert (`role="alert"`) | The server's message after a refusal (400, 409 duplicate code, 409 no encryption key); the dialog stays open |
| **Add gateway** / **Save**; **Cancel** | Add sends `{definition, environment, values}` once (only values that were typed); Save sends the definition only. Success closes the dialog, shows the toast "{name} was added." or "{name} was updated." and reloads the list and overview. Both buttons are disabled while saving |

Validation messages (`admin.paymentGateway.form.errors.*`, in this order of checking): "Enter a gateway name."; "The code must be 2 to 30 capital letters, digits or underscores, starting with a letter." (Add only); "Enter at least one currency."; "Choose at least one payment method."; "Every credential needs a label and a name (letters and digits, starting with a letter)."; "Two credentials have the same name."; "Mark at least one credential as required." The server re-checks everything and also refuses currencies that are not three letters, countries that are not two, over-long text and more than 12 credentials ([API](../../06-api/api-requirements/payment-gateways.md#gateways-added-by-an-administrator-c99)); its message is shown in the error alert.

### Remove confirmation
**Remove** opens a dialog titled "Remove {name}?" with "The gateway and its stored credentials are deleted. This cannot be undone.", **Cancel** and a red **Remove**. Confirming calls `DELETE`, shows "{name} was removed." and reloads the list; Cancel changes nothing. Razorpay has no Remove or Edit button.

## Configure dialog
Opened by **Configure**; title "Configure {name}"; loads `GET /admin/billing/gateways/{code}`. **The form is generated from the provider's field list** (for an added gateway, the credential fields of its definition: it is how values are entered or rotated after the gateway was added; a blank field keeps the stored value and no connection test is run).

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
Opened by **View details**; title is the provider logo and name; tabs (accessible name "Gateway details"): **Overview**, **Webhook**, **Activity**. **A gateway added by an administrator has Overview and Activity only** (no Webhook tab: it has no endpoint), starts with the info alert "{name} was added by an administrator. Its credentials are stored encrypted, but EIS has no payment integration for it, so it cannot be tested, activated or used for payments.", lists its payment methods as plain chips (no switches), shows each credential as Secret or Identifier with Set (stored here) / Not set, and does **not** show "Verified by real activity".

**Overview:** the description; "Countries and currencies"; "Supports" chips (environments, Webhooks, Refunds, Recurring payments); **Payment methods**: one switch per supported method (an added gateway: plain chips), the last method still on cannot be switched off, with the help "Switch off a method to stop offering it through this gateway. At least one must stay on."; **Configuration fields** with the kind (Secret or Identifier) and Set (source) / Not set per field; **Verified by real activity**: four lines, *Credentials accepted*, *Signed webhook received*, *Payment captured*, *Refund made*, each "Yes" (check icon) or "Not yet", with the note that a passing test does not prove payments, webhooks or refunds work ([BR-16](../../02-requirements/FRD/payment-gateways/business-rules.md)).

**Webhook:** the warning "This URL points at this machine, so the provider cannot reach it. Set the public backend address (BACKEND_URL) for this environment." when `reachableFromInternet` is false; the **Webhook URL** in a read-only box with a **Copy** button ("Copy the webhook URL", tooltip Copy / Copied); chips: Webhook secret set / missing, Signature verified / No signed event yet / No webhooks, and "{n} rejected webhook(s)" when there are any; "Last event received: {type · date}" or Never; a warning with the last rejected webhook's date and reason; "Subscribe the webhook to" with the event names; **Set-up steps** as a numbered list from the provider.

**Activity:** "Recent webhook events" (up to 8: date, event type, payment number) or "No webhook has been received."; "Changes by administrators" (the last 10 audit entries: date, text, actor) or "No changes recorded yet."; "Configuration version {n}" and when the credentials were last updated. **Close** button.

## States
| State | Behaviour |
|---|---|
| Loading | A skeleton card block; each dialog shows a skeleton until its data loads |
| Load error | The server's message (or "Could not load the payment gateways.") in an error alert with **Retry** |
| Empty | The registry always has at least one provider (Razorpay); added gateways appear below it |
| Action error | In the card's row notice (list actions, Remove) or in the dialog (add, edit, configure, methods) |
| No encryption key | Configure dialog warning, Save disabled; credentials from the secrets file keep working |
| Local webhook URL | Warning on the Webhook tab |

## Accessibility
WCAG 2.1 AA; automated axe check on the page (`AdminPaymentGatewayPage.test.tsx`). Each card is a list item named by the gateway; every action button has an accessible name that includes the provider name; the overview is a description list; status chips carry text (never colour alone); dialogs are labelled by their titles and trap focus; the tab set has an accessible name; row notices are announced (`role="status"`) and the working indicator has the accessible name "Working"; secret inputs are password fields that are never prefilled. Keyboard: every control is reachable and has a visible focus indicator.

## i18n
Every string is under `admin.paymentGateway` in `frontend/src/i18n/locales/en.json` and `es.json` (the sidebar label is `appShell.nav.paymentGateway`). Groups: `title`, `subtitle`, `stats.*`, `configuration.*` (status labels), `connection.*` (the eight connection statuses), `env.*`, `methods.*`, `source.*` (STORED, ENVIRONMENT, NONE), `tabs.*`, `cap.*`, `evidenceItems.*`, `signature.*`, `form.*` (the Add and Edit dialog: labels, help, `errors.*`, `added`, `updated`); and the single keys for the buttons and their accessible names (`configure`, `configureNamed`, `testConnection`, `testNamed`, `activate`, `activateNamed`, `deactivate`, `deactivateNamed`, `makeDefault`, `makeDefaultNamed`, `viewDetails`, `viewDetailsNamed`, `edit`, `editNamed`, `remove`, `removeNamed`), the notices (also `removeTitle`, `removeBody`, `removed`, `noIntegrationChip`, `configurationOnlyLine`, `configurationOnlyNote`) (`activated`, `deactivated`, `defaultSet`, `saved`, `saveFailed`, `testFailed`, `loadError`), and the dialog texts (`configureHelp`, `rotationNote`, `noEncryptionKey`, `unreadable`, `keepSecret`, `fieldSet`, `fieldNotSet`, `current`, `localUrl`, and so on). Provider names, descriptions, field labels and set-up steps come from the backend (for an added gateway, from what the administrator typed) and are not translated by the screen. A method code without a `methods.*` key shows the code.

## Known discrepancy
The Webhook tab's local-address warning names the variable `BACKEND_URL`, which the backend reads as the property `app.backend-url` (`backend/src/main/resources/application.yml`). Operators must set `BACKEND_URL` to the public backend address.

## Not on this screen
Switching Razorpay keys by editing `config/secrets.env` still works (BR-5) but is not described here: see [09-integrations/razorpay.md](../../09-integrations/razorpay.md). Routing by country or amount, fail-over and recurring charges are Not specified ([C98](../../01-business/roadmap/open-decisions.md#c98)).

## API used
`GET /admin/billing/gateways`, `GET /admin/billing/gateways/summary`, `GET /admin/billing/gateways/templates`, `POST /admin/billing/gateways`, `PUT …/{code}/definition`, `DELETE …/{code}`, `GET /admin/billing/gateways/{code}`, `PUT …/{code}/configuration`, `POST …/{code}/test`, `POST …/{code}/activate`, `POST …/{code}/deactivate`, `PUT …/{code}/methods`, `PUT /admin/billing/gateways/default` — all in [payment-gateways.md](../../06-api/api-requirements/payment-gateways.md).

## Built
2026-10-14 (C98; the Add, Edit and Remove dialogs and the removal of Planned integrations, C99, the same day). Tests: `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx`; test cases in [`test-cases/functional/payment-gateways/`](../../../test-cases/functional/payment-gateways/TESTPLAN-BIL-003.md).

> **History.** The C60 version of this screen (a read-only Razorpay panel with the setup checklist and keys in `config/secrets.env`) and the C97 BI-8 gateway list (Billing settings → Payment gateways) are replaced by this screen.
