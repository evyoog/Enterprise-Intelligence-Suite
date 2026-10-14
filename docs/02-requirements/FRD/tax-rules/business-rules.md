# Business rules — Tax Rules and Calculation (REQ-BIL-002)

Cross-feature rule that also applies: [BR-SEC-001 Central secrets file](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

## Engine choice
| ID | Rule |
|---|---|
| BR-1 | Exactly one engine is active for new calculations: **Internal** (switch off, the default) or **External** (switch on). The choice is one row in the database (`tax_settings`) and is read by every server. |
| BR-2 | A calculation reads the engine **once**, at its start, and keeps it to the end including retries (pinned configuration). The invoice stores the engine and the configuration version. |
| BR-3 | There is **no fall-back** between engines. Internal mode never calls the provider; external mode never reads a tax rule. |
| BR-4 | External mode can be switched on only when a provider is selected, its credentials are present, and the validation checks pass in the same request. Editing the provider settings while external mode is on is accepted only if the new settings pass validation too. |
| BR-5 | The switch changes **future** calculations only. Issued invoices, their tax lines and credit notes are never recalculated. |
| BR-6 | Only a platform administrator (`MANAGE_BILLING`) may change the switch, rules, classes, exemptions, registrations and provider settings. |

## Rules (internal engine)
| ID | Rule |
|---|---|
| BR-10 | No rate is written in code, in the frontend or in a migration. Every rate comes from an approved rule. |
| BR-11 | A rule is used only when it is **Approved**, **active**, and its effective-from date is on or before the invoice date and its effective-to date (if any) is on or after it. The invoice date is the date of the calculation in UTC. |
| BR-12 | A rule matches when its country equals the customer's country; its state, if set, equals the place of supply; its tax class, if set, equals the line's class; its customer type, supply type and tax ID condition are Any or equal to the transaction's. A condition that cannot be decided from the data (for example the supply type when the state is missing) matches only a rule where it is Any. |
| BR-13 | **Customer type:** Business when the invoice owner is an organization, Individual when it is an individual customer. **Tax ID condition:** Provided when the billing details have a tax ID. |
| BR-14 | **Supply type:** Cross-border when the customer's country differs from the seller's country. Within the same country: Within state when the seller's state equals the place of supply, Between states when they differ, undecided when either is missing. For India the place of supply is the state coded in the customer's GSTIN when the GSTIN format is valid, otherwise the customer's billing state. |
| BR-15 | The most specific matching rule wins. Specificity weights: state 16, tax class 8, supply type 4, customer type 2, tax ID condition 1. Two matching rules from different rule families with the same weight are **ambiguous** and block the calculation. |
| BR-16 | A taxable rule needs at least one component. A non-taxable treatment (Exempt, Zero-rated, Nil-rated, Reverse charge, Out of scope) has no components and produces an explicit zero-tax line that records the treatment; it is never confused with "no rule". |
| BR-17 | A component amount = taxable amount × rate ÷ 100, rounded half up to the currency's smallest unit, **per line and per component**. A compound component uses the taxable amount plus the amounts of the components before it. The line tax is the sum of its components; the invoice tax is the sum of the lines. |
| BR-18 | If no rule matches, or the customer's country or the seller's country is missing, the result is **Blocked** with a reason. Zero tax is only ever the result of a zero rate, an exemption or a non-taxable treatment. |
| BR-19 | A customer **exemption** applies when it is Approved, in force on the invoice date and its country equals the customer's country. The result is Exempt with the reason and certificate number; no component is charged. |

## Rule lifecycle and versions
| ID | Rule |
|---|---|
| BR-20 | States: **Draft → Pending approval → Approved** or **Rejected** (back to editing). An Approved rule can be activated or deactivated. Only Draft and Rejected rules can be edited or deleted. |
| BR-21 | **New version** of an Approved rule creates a Draft with the same rule family and the next version number. When it is approved, the previous version's end date is set to the day before the new start date. A new version must start after the previous version started. |
| BR-22 | **Maker and checker:** the administrator who last edited or submitted a rule (or exemption) cannot approve or reject it, unless the platform setting "Allow the same administrator to approve" is on. |
| BR-23 | Approval is refused when another Approved rule of a different family has the same scope (country, state, class, customer type, supply type, tax ID condition) and overlapping dates. |
| BR-24 | Effective-to must not be before effective-from. Component codes are unique within a rule. Rates are 0 to 100 with up to 4 decimals. |

## External provider
| ID | Rule |
|---|---|
| BR-30 | Provider credentials (base URL, API key, API secret) come from the backend environment. They are never stored in the database, never returned by an API, never logged and never shown. The screen shows only whether each value is set and the masked host. |
| BR-31 | A call is sent only when the provider is configured, its last validation is **Passed** and the capabilities it reported include the customer's country, the transaction currency and the transaction type. |
| BR-32 | The request carries the supplier's legal entity and tax registrations, the customer's billing details and tax IDs, each line's tax class and taxable amount, the currency, the date and a unique transaction reference. The transaction reference is also sent as the idempotency key. |
| BR-33 | A response is valid only when: status is success; the transaction reference equals the request's; the currency equals the request's; every request line comes back once with the same taxable amount; every component amount is zero or more; each line's tax is the sum of its components; the total tax is the sum of the lines and not more than the taxable total; a provider reference is present. |
| BR-34 | Retry only transient failures (network error, timeout, 429, 5xx), at most the configured attempts (default 3, maximum 5), waiting 250 ms, 500 ms, 1 s … between attempts. |
| BR-35 | The stored calculation keeps the provider reference and a copy of the validated response. It never keeps a credential. |

## Invoices and credit notes
| ID | Rule |
|---|---|
| BR-40 | An invoice is **finalized** (number, issue date, due date, status Open, event published) only when its tax calculation succeeded. Otherwise it stays **Draft** with the block code and message. A Draft cannot be paid, downloaded or shown to the customer as an invoice. |
| BR-41 | Retrying a Draft restarts the calculation with the engine in force at that moment. |
| BR-42 | A credit note reverses the original invoice's tax lines in proportion to the credited share of the invoice total. Rounding differences are given to the lines with the largest remainders so the credit note's tax equals the credited gross minus the credited net exactly. The total credited for an invoice never exceeds its total. |
| BR-43 | Plan prices are **without tax** (TX-1). A zero-amount plan creates no invoice and needs no tax calculation. |
| BR-44 | **Validate tax:** a tax ID field is checked by format only: a 15-character GSTIN pattern for Indian customers; other countries accept free text up to 50 characters (TX-4). |
