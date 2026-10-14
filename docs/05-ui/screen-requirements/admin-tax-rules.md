# Screen: Tax configuration

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) (decisions [C96](../../01-business/roadmap/open-decisions.md#c96), [C97](../../01-business/roadmap/open-decisions.md#c97)) |
| Route | `/admin/billing/tax`. The tab is kept in the URL: `?tab=rules` (default, omitted), `classification`, `exemptions`, `registrations`, `provider`, `blocked`. The Draft invoices tab is also reached from **Resolve tax** on a Draft row of Admin → Billing (invoices and payments) |
| Menu | Platform Admin → Billing → **Tax configuration** (sidebar item next to Invoices & payments, Payment gateway and Billing settings; label key `nav.taxConfiguration`) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). The sidebar item is hidden for others and every tax API call answers 403 |
| Implementation | `frontend/src/pages/admin/AdminTaxConfigurationPage.tsx`, panels in `frontend/src/components/tax/`, API client `frontend/src/api/taxApi.ts` |
| API | [tax-rules.md](../../06-api/api-requirements/tax-rules.md) (`/api/admin/billing/tax/...`) |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). The screen never shows a provider key, secret or full address: it only shows **Set / Not set** and the provider host ([BR-30](../../02-requirements/FRD/tax-rules/business-rules.md), [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md)).

## Layout
1. Page header (eyebrow "Billing", title "Tax configuration", subtitle).
2. **Engine card** (always visible, above the tabs).
3. Tab bar (scrollable on narrow screens) and the selected panel.
4. A snackbar (4 seconds) confirms every successful save or action.

## Engine card (REQ-BIL-002.1 to .6)
| Element | Behaviour |
|---|---|
| **External Tax Engine** switch | Label "External Tax Engine", description **"Enable or disable external tax calculation for billing transactions."** Off (default) = EIS internal engine; On = external provider. The switch shows its state as text ("External Tax Engine: On/Off") and has the accessible name "External Tax Engine". Changing it opens a confirmation dialog first (not destructive style): switching **on** says EIS tests the provider first, issued invoices do not change and a failing provider leaves invoices as Draft with no fall-back to EIS rules; switching **off** says new invoices use the approved EIS rules. The switch is disabled while the request runs |
| Helper line | One sentence for the current mode (provider is used and there is no fall-back / approved rules are used) |
| Active calculation mode | "EIS internal tax engine" or "External tax provider" |
| Status chip | **Ready** (green), **Needs attention** (amber), **Blocking invoices** (red). Icon and text; colour is never the only signal |
| Version and change line | "Configuration version N" and "Changed <date time> by <administrator>", or "Not changed yet: this is the default." |
| Messages | One outlined alert per message the server returns (info, warning or error): for example no approved rule in force, provider not validated, invoices waiting for tax |
| Refused switch-on dialog | When the server answers 409 the setting stays Off and a dialog "The external engine was not switched on" lists **every** check with a tick or cross and its detail (provider selected, credentials, connection, supplier entity, supplier registration, country support, currency support, transaction type). **Close** dismisses it |
| Switch error | A failed call (for example network) shows an error alert in the card and the switch is unchanged |

Switching off is always allowed (REQ-BIL-002.5); if no approved rule is in force, a warning message in the card says invoices will be blocked.

## Tabs
| Tab (i18n `admin.tax.tabs.*`) | Content | Requirement |
|---|---|---|
| **Rules** (default) | Rules table with approval workflow and versions, **Allow the same administrator to approve** policy switch, rule dialog, reject dialog, delete confirmation, and the **preview calculator** below the table | .10 to .14, .17, .21 |
| **Classification** | Tax classes table and dialog; product classification table | .19 |
| **Exemptions** | Exemption certificates table and dialog | .18 |
| **Registrations** | Supplier tax registrations table and dialog | .37 |
| **External provider** | Provider settings, credentials (set / not set), Test connection and capability validation | .30 to .34 |
| **Draft invoices** | Invoices blocked for tax, with the reason and **Retry tax**. The tab label carries a red count badge when invoices are waiting | .42 |

### Rules tab
**Table** columns: Rule (name, "Version N", tax type), Where (state and country, jurisdiction), Conditions (supply type, customer type, tax ID condition, tax class, or "Any"), Components (for example "CGST 9% + SGST 9%", or the treatment for a non-taxable rule), Effective (from, until or "no end"), Status, Actions. Older versions that are neither latest nor in force are dimmed.

**Status chip:** Draft (neutral), Pending approval (amber), Approved (green), Rejected (red, with the rejection reason below it). An Approved rule also shows chips **Active / Inactive** and **In force / Not yet in force**.

**Row actions** by status ([BR-20](../../02-requirements/FRD/tax-rules/business-rules.md)):
| Status | Actions |
|---|---|
| Draft, Rejected | Edit, Submit for approval, Delete (confirmation dialog) |
| Pending approval | Approve, Reject (dialog asks for a required reason) |
| Approved | Deactivate or Activate; **New version** (only on the latest version, copies the rule into a Draft with the next version) |

A refused action (the same administrator approving, an overlapping approved rule, a wrong state) shows the server's own message in a dismissible error alert above the table. While an action runs the row shows a small progress indicator and its buttons are disabled.

**Approval policy:** a switch "Allow the same administrator to approve" (off by default) with the help "Off by default: a rule must be approved by a different administrator than the one who edited it." ([BR-22](../../02-requirements/FRD/tax-rules/business-rules.md)).

**Rule dialog** (create and edit; only Draft or Rejected rules):
| Field | Type | Required | Validation (client; the server validates again) | i18n key (`admin.tax.ruleForm.`) |
|---|---|---|---|---|
| Name | Text | Yes | Not blank | `name`, `errors.name` |
| Country | Select, from the server's reference list | Yes | Chosen | `country`, `errors.country` |
| State or province | Select (India, US, Canada, Australia) or free text; blank = anywhere in the country | No | Known state when a list exists | `state`, `stateHelp` |
| Jurisdiction | Text | No | | `jurisdiction` |
| Tax type | Select: GST, VAT, Sales tax, Other | Yes | | `taxType` |
| Tax class | Select of enabled classes, or "Any class" | No | | `taxClass`, `anyClass` |
| Treatment | Select: Taxable, Exempt, Zero-rated, Nil-rated, Reverse charge, Out of scope | Yes | | `treatment` |
| Supply type | Select: Any, Within state, Between states, Cross-border | Yes (default Any) | | `supplyType` |
| Customer type | Select: Any, Individual, Business | Yes (default Any) | | `customerType` |
| Customer tax ID | Select: Any, Provided, Not provided | Yes (default Any) | | `taxIdCondition` |
| Effective from | Date | Yes | Chosen (defaults to today) | `effectiveFrom`, `errors.from` |
| Effective to | Date | No | Not before effective from | `effectiveTo`, `errors.to` |
| Notes | Multiline text | No | | `notes` |
| Components (taxable only) | Rows of code, name, rate, "compound" checkbox; **Add component**; remove button per row (disabled when one row is left) | At least one | Code: letters, digits, `_`, unique; name not blank; rate 0 to 100 with up to 4 decimals; shows the total rate | `components`, `componentCode`, `componentName`, `componentRate`, `compound`, `errors.*` |

For a non-taxable treatment the component rows are replaced by a note that no components are needed ([BR-16](../../02-requirements/FRD/tax-rules/business-rules.md)). Errors show after the first **Save** attempt. A server error shows in an alert at the bottom of the dialog. No rate is pre-filled anywhere ([BR-10](../../02-requirements/FRD/tax-rules/business-rules.md)).

**Preview calculator** (REQ-BIL-002.21): fields Amount (decimal, in major units, converted to minor units), Currency (INR, USD, EUR, GBP), Country, State, Tax class, Customer type (Individual, Business), Customer tax ID; button **Calculate** calls `POST /preview`. Result: the place of supply, one row per component (name, rate, tax amount), total tax and total; or a warning "Blocked: <reason>" when no rule matches. It creates nothing and uses the internal rules only, even when external mode is on.

### Classification tab
- **Tax classes**: table (code, name, SAC/HSN code, Active/Inactive, number of products) with **New class** and **Edit**. The dialog has Code (letters, digits, `-`, `_`, up to 40; locked after creation), Name (required), SAC/HSN code, Description, Enabled.
- **Products**: table of every product with a select of enabled classes (plus "No class"); a change is saved immediately and confirmed by a toast. A product without a class is blocked by the external engine ([BlockCode](../../06-api/api-requirements/tax-rules.md) `MISSING_TAX_CLASS`).

### Exemptions tab
Table: Customer or organization (name, type and id), Country, Reason (and certificate number), Effective, Status (Pending approval, Approved, Revoked), actions **Approve** (pending, by another administrator) and **Revoke**. **New exemption** dialog: Owner type (Customer, Organization), Owner id (number), Country, Reason (required), Certificate number, Effective from (required), Effective to.

### Registrations tab
Table: Country (and state), Tax type, Number (monospace), Effective dates, with Edit and Delete (confirmation) for rows created here. The business profile's GSTIN is listed as a read-only row with a "From business profile" chip. **New registration** dialog: Country, State, Tax type, Registration number (required), Effective from and to, Enabled.

### External provider tab
Three sections:
1. **External tax provider** (settings): Provider (select of adapters the server offers, for example the generic REST adapter), Company code (optional), Environment (Sandbox or Production), Timeout in seconds (2 to 60), Attempts per calculation (1 to 5, help: only temporary failures are retried with the same idempotency key). **Save settings** is disabled until a provider is chosen and the numbers are valid. While external mode is on, an info note says new settings are saved only if the provider still passes validation; a refused save shows the server's message.
2. **Credentials on the server**: one chip per backend environment variable (`TAX_PROVIDER_BASE_URL`, `TAX_PROVIDER_API_KEY`, `TAX_PROVIDER_API_SECRET`) saying **Set** or **Not set**, and the provider host. There is **no field** to type or reveal a key or secret.
3. **Last validation**: status chip (Not validated, Passed, Failed), the time and summary, the button **Test connection and validate capabilities** (disabled without a provider; shows "Testing…" while running), the list of every check with tick or cross after a test, and the supported countries, currencies and transaction types as chips.

### Draft invoices tab
Explains that an invoice whose tax could not be determined stays a Draft: no number, cannot be paid and is not shown to the customer. Table: Invoice ("Draft #id"), Customer, Amount (subtotal), Reason (block code label, with the server's message under it), Engine (internal or external), and **Retry tax**. After a retry the invoice is either issued (toast "Invoice <number> was issued.") or a warning says "Still blocked: <reason>". The engine card status is refreshed after every retry.

## States
| State | Behaviour |
|---|---|
| Loading | Page: a skeleton block under the header until status and reference data arrive. Each tab: a skeleton matching its table |
| Empty | Rules: "No tax rules yet. Create one for each country, state and condition you charge tax in. Until a rule is approved, invoices wait as Draft." (`admin.tax.rules.empty`); classes, exemptions, registrations, Draft invoices ("No invoices are waiting for tax.") each have their own empty message. Provider: "The provider has not been validated yet." |
| Error | The server's message, or "Tax configuration could not be loaded.", in an error alert with **Try again**. An action error (approve, retry, save) shows in an alert without hiding the data |
| Blocked | Status chip "Blocking invoices" and an error alert in the card; the Draft invoices tab shows a red count badge; a Draft is listed with its block code and reason. Block codes shown: No matching rule, Ambiguous rules, Customer country missing, Business profile incomplete, Product has no tax class, Provider not configured, Provider not validated, Not supported by the provider, Provider unavailable, Provider rejected the request, Invalid provider answer, Calculation error |
| Refused | The switch-on refusal dialog lists every failed check; the switch stays Off |

## Accessibility
- The engine card, each panel and each settings section are named regions (`aria-labelledby` the section heading). The tab bar has the label "Tax configuration sections"; each tab has `aria-controls` and the panel is a `role="tabpanel"` labelled by its tab.
- The switch has an accessible name and a visible text state; status uses icon plus text; tick and cross icons are decorative (`aria-hidden`) with the check label and detail as text.
- Tables have an accessible name; icon-only buttons (remove component) have `aria-label`; the product class selects are labelled "Tax class for <product>".
- Every dialog has a title (`aria-labelledby`) and returns focus on close; the reject reason field takes focus on open; error alerts use `role="alert"`.
- Forms show errors as text under the field after the first attempt. The page passes the axe check (`AdminTaxConfigurationPage.test.tsx`).
- Layouts are single-column below the small breakpoint; the tab bar scrolls.

## i18n
All user-facing text is under the `admin.tax` key in `frontend/src/i18n/locales/en.json` and `es.json` (`engine`, `tabs`, `rules`, `ruleForm`, `taxType`, `treatment`, `customerType`, `supplyType`, `taxIdCondition`, `preview`, `classes`, `exemptions`, `registrations`, `provider`, `blocked`), plus the sidebar label `nav.taxConfiguration` and the Billing link `admin.billing.resolveTax`. Dates and times use the active language. Server messages (block reasons, validation details) arrive in the API's language and are shown as they are.
