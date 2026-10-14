# REQ-BIL-002 — Tax Rules and Calculation

**Status:** Approved (product owner request and answers TX-1 to TX-6, 2026-10-13; replaces the 2026-10-01 draft)
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner, 2026-10-13 ([C96](../../../01-business/roadmap/open-decisions.md#c96), [C97](../../../01-business/roadmap/open-decisions.md#c97))
**Decision:** [C51](../../../01-business/roadmap/open-decisions.md#c51) (original), changed by [C96](../../../01-business/roadmap/open-decisions.md#c96)

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-BIL-002 |
| Application | [08 Billing & Payments](../../../01-business/roadmap/applications/08-billing-payments.md) |
| Application code | `APP-BIL` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 08.05.01 | Configure tax rules | Yes |
| 08.05.01 | Calculate tax | Yes |
| 08.05.01 | Validate tax | Yes (customer tax ID format, TX-4) |
| 08.02.02 | Credit invoice / 08.04.01 Generate credit note | Yes (TX-6, tax part) |

## Summary
Every invoice needs a tax amount. The platform administrator chooses **one calculation engine for all new tax calculations** with a switch on **Platform Admin → Billing → Tax Configuration**:

| Switch **External Tax Engine** | Engine | Where the rates come from |
|---|---|---|
| **Off** (default) | EIS **internal** engine | Region-based rules the platform administrator configures, approves and activates in EIS. No rate is written in code. |
| **On** | **External** tax provider | A provider connected through an adapter layer. Its credentials stay on the backend. |

A calculation either produces a valid tax breakdown or it **blocks the invoice**. There is no default rate, no silent zero tax and no silent fall-back from one engine to the other. Historical invoices never change when the switch moves ([C96](../../../01-business/roadmap/open-decisions.md#c96)).

## Actors
- Platform administrator (`MANAGE_BILLING`): switch the engine, manage rules, tax classes, exemptions, registrations and provider settings, approve rules, retry blocked invoices
- Second platform administrator (`MANAGE_BILLING`): approves rules and exemptions created by someone else (maker and checker)
- `InvoiceService` (system): calls the tax calculation when it generates an invoice or a credit note

## Functional requirements

### Engine switch and status
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-002.1 | **Platform Admin → Billing → Tax Configuration** has the setting **External Tax Engine** ("Enable or disable external tax calculation for billing transactions") as a switch. On = external provider, Off = EIS internal engine. | Must |
| REQ-BIL-002.2 | The screen always shows the **active calculation mode**, its **status** (Ready, Needs attention, Blocking invoices), who changed it and when, and the numbers that explain the status (approved rules in force, validation result of the provider, invoices waiting for tax). | Must |
| REQ-BIL-002.3 | The setting is stored in the database. It survives sign-out, application restart and deployment, and applies to every administrator and every server. | Must |
| REQ-BIL-002.4 | **External mode cannot be switched on** until a provider is selected, its credentials are present on the backend and the provider passes the validation checks (connection, authentication, support for the supplier's country and for the currencies in use). The switch-on request runs the checks itself; a failure leaves the setting unchanged and lists every failed check. | Must |
| REQ-BIL-002.5 | Switching **off** is always allowed. The screen warns when no approved internal rule is in force, because invoices would then be blocked. | Must |
| REQ-BIL-002.6 | Every change of the setting is audited (`TAX_ENGINE_MODE_CHANGED`) with the previous and new mode and the acting administrator. | Must |

### Internal engine (switch off)
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-002.10 | A platform administrator can create, edit, submit, approve, reject, activate, deactivate and version **tax rules** with: country, state or province (optional), jurisdiction name, tax type (GST, VAT, Sales tax, Other), product or service tax class (optional), customer type (Any, Individual, Business), supply type (Any, Within state, Between states, Cross-border), customer tax ID condition (Any, Provided, Not provided), treatment (Taxable, Exempt, Zero-rated, Nil-rated, Reverse charge, Out of scope), effective-from and optional effective-to date. | Must |
| REQ-BIL-002.11 | A **taxable** rule has one or more **components** (for example CGST and SGST, or IGST, or a state and a county tax), each with a code, a name, a rate (0 to 100, up to 4 decimals) and an optional "compound" flag (the component is charged on the base plus the components before it). A non-taxable treatment has no components. One invoice line can carry several components. | Must |
| REQ-BIL-002.12 | Only rules that are **Approved**, **active** and **in force on the invoice date** and that **match the transaction** are used. | Must |
| REQ-BIL-002.13 | **Versioning:** an approved rule is never edited. "New version" copies it as a draft with the next version number; approving the new version closes the previous one the day before the new one starts. Earlier versions stay readable. | Must |
| REQ-BIL-002.14 | **Maker and checker:** the administrator who last edited a rule cannot approve it, unless the platform setting "Allow the same administrator to approve" is on (off by default). Approval is refused when another approved rule has the same scope and overlapping dates. | Must |
| REQ-BIL-002.15 | **India GST:** for a customer in India the engine decides **within state** or **between states** by comparing the seller's state (Billing settings → business profile) with the place of supply (the state of the customer's GSTIN when it is valid, otherwise the customer's billing state). Within state uses the rule with components CGST and SGST (or UTGST); between states uses the rule with IGST. The rules and their rates are configured, not coded. | Must |
| REQ-BIL-002.16 | **Other countries** use the same rule structure with their own components, jurisdiction and conditions. Nothing assumes one rate per country. | Must |
| REQ-BIL-002.17 | When several rules match, the **most specific** wins (state, then tax class, then supply type, then customer type, then tax ID condition). Two different rules with the same specificity make the calculation **ambiguous** and block the invoice. | Must |
| REQ-BIL-002.18 | A **tax exemption** (certificate) can be recorded for a customer or organization and a country, with a reason, certificate number and dates; after approval it makes the calculation return an explicit **Exempt** result with the reason. Other treatments (zero-rated, nil-rated, reverse charge, out of scope) are rule treatments. | Must |
| REQ-BIL-002.19 | **Tax classes** (for example a SAC code) are maintained by the administrator and set on each product; rules can be limited to a class. | Must |
| REQ-BIL-002.20 | If **no rule matches**, or the data needed to decide is missing (customer country, the seller's country, the place of supply for a rule that depends on it), the calculation is **blocked** with a reason. No default rate and no zero tax are assumed. | Must |
| REQ-BIL-002.21 | A **preview** lets an administrator try an amount, country, state, class and customer type and see the components, or the block reason, without creating an invoice. | Should |

### External engine (switch on)
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-002.30 | The billing module talks to a **provider adapter interface**, not to a vendor. Adding a provider means adding one adapter. The first adapter is a generic REST adapter that speaks the contract in [tax-rules API](../../../06-api/api-requirements/tax-rules.md#external-provider-contract-v1). | Must |
| REQ-BIL-002.31 | The calculation workflow is: (1) read the supplier's legal entity and tax registrations, (2) read the customer's billing details and tax IDs, (3) read each product's tax class, the taxable amount and the currency, (4) send the transaction to the provider, (5) receive the breakdown, (6) **validate** the response (status, transaction reference, currency, taxable amounts per line, arithmetic, no negative tax), (7) store the result and the provider reference, (8) generate the invoice with the validated result. | Must |
| REQ-BIL-002.32 | Provider credentials (base URL, API key, API secret) live **only** in the backend environment ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)). No API returns or shows a secret; the screen only reports **set / not set** and a masked host. | Must |
| REQ-BIL-002.33 | **Test connection** and **Validate capabilities** buttons call the provider and show the result. The provider's supported countries, currencies and transaction types are stored with the validation result. | Must |
| REQ-BIL-002.34 | Before every call the engine checks that the provider supports the customer's country, the currency and the transaction type. An unsupported combination blocks the invoice. | Must |
| REQ-BIL-002.35 | **Transient failures** (network error, timeout, HTTP 429 and 5xx) are retried up to the configured number of attempts (default 3) with growing waits, always with the same idempotency key. A permanent failure (other 4xx) or an invalid response is not retried. | Must |
| REQ-BIL-002.36 | When the provider cannot give a valid calculation the invoice is **blocked**. The platform **never falls back to the internal engine** while the switch is on. | Must |
| REQ-BIL-002.37 | **Supplier tax registrations** (country, state, tax type, number, dates) are kept in EIS, together with the business profile's GSTIN, and sent to the provider. | Must |

### Switching, invoices and credit notes
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-002.40 | The switch controls only **new** calculations. An issued invoice, its tax lines and its credit notes never change when the switch moves. | Must |
| REQ-BIL-002.41 | A calculation reads the mode **once** and uses it to the end (a pinned configuration), including provider retries. A switch made meanwhile does not affect it. The invoice records the engine and configuration version it used. | Must |
| REQ-BIL-002.42 | An invoice whose tax cannot be determined safely is created as **Draft**: it has no number, cannot be paid, is not shown to the customer as an invoice and records the **block reason**. A platform administrator fixes the cause (a rule, the provider, billing details) and presses **Retry tax**. A retry **restarts** the calculation with the mode in force at that moment; success finalizes the invoice (number, issue date, due date, event). | Must |
| REQ-BIL-002.43 | The rate, component, taxable amount, tax amount, rule or provider reference and tax class used are copied to the invoice as **tax lines**. Later rule changes never change them ([C51](../../../01-business/roadmap/open-decisions.md#c51)). | Must |
| REQ-BIL-002.44 | Prices are **without tax**; tax is added on the invoice (TX-1). The invoice shows the place of supply and, per line, the tax class and SAC/HSN code. | Must |
| REQ-BIL-002.45 | **Validate tax** checks the **format** of the customer's tax ID, for example a 15-character GSTIN for an Indian customer (TX-4). An invalid ID in a field that has a format rule is refused when billing details are saved. A valid GSTIN does not change the tax for Indian customers and is printed on the invoice (TX-5). | Must |
| REQ-BIL-002.46 | **Credit notes** are built with tax (TX-6): a refund issues a credit note that reverses the original invoice's tax lines in proportion, never recalculating with the engine in force today. Credit notes have their own number series. | Must |
| REQ-BIL-002.47 | Every rule, class, exemption, registration, setting and provider-validation change is written to the audit log; so is every blocked or finalized calculation. | Must |

## Out of scope
- Which external provider EIS contracts with. The adapter layer is built; vendor-specific adapters are added when a provider is chosen ([C96](../../../01-business/roadmap/open-decisions.md#c96)).
- Reporting credit notes or voids back to the external provider (the provider is asked only to calculate).
- Amount thresholds on a rule, withholding tax, tax returns and filing, e-invoicing (IRN, QR code), Tax-inclusive pricing (TX-1 A: prices are without tax).
- Multi-jurisdiction tax where one customer is taxed by several regions at once, other than several components of one rule.
- Currency conversion of tax amounts.

## Dependencies
- [REQ-BIL-001](../billing-payments/requirement.md) invoices, payments and Billing settings (business profile, GSTIN, state).
- Billing details (`billing_details.country`, `state`, `tax_id`) from REQ-BIL-001.1.
- Products get a tax class (`products.tax_class_code`).
- Data model: [07-database/data-model/tax-rules.md](../../../07-database/data-model/tax-rules.md), migration V036.
- Permission `MANAGE_BILLING` (platform administrator). No new permission.
- Common secrets file: [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), business rules, workflow, acceptance criteria | this folder |
| Screen | [docs/05-ui/screen-requirements/admin-tax-rules.md](../../../05-ui/screen-requirements/admin-tax-rules.md) |
| API | [docs/06-api/api-requirements/tax-rules.md](../../../06-api/api-requirements/tax-rules.md) |
| Data model | [docs/07-database/data-model/tax-rules.md](../../../07-database/data-model/tax-rules.md) |
| Cross-feature rules | [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md) |

## Open questions
| # | Question | Blocks |
|---|---|---|
| 1 | Which external tax provider will be contracted? (Adapters for a named vendor are added then.) | No |
| 2 | Tax treatment of foreign customers beyond "configure a rule for the country" (TX-5: "ask your accountant"). EIS ships **no** rate; the accountant's rules are entered by the administrator. | No |
| 3 | Should a blocked (Draft) invoice also stop the purchased subscription from activating? Today the subscription is already active when the invoice is generated; BI-1 changes activation for card and UPI. | No |
