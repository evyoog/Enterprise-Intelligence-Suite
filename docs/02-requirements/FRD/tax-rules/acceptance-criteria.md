# Acceptance criteria — Tax Rules and Calculation (REQ-BIL-002)

Test cases are in `test-cases/functional/tax-rules/` (TC-BIL-0xx).

| ID | Requirement | Given / When / Then |
|---|---|---|
| AC-1 | .1, .2, .3 | **Given** Platform Admin → Billing → Tax Configuration **then** the External Tax Engine switch, the active mode (Internal or External) and its status are shown. **When** the administrator changes the switch, signs out and back in, or the application restarts **then** the same mode is shown. |
| AC-2 | .4 | **Given** no provider credentials on the backend **when** the administrator switches External on **then** it is refused, the setting stays Off and the failed checks are listed. **Given** a provider that passes every check **when** switching on **then** the mode becomes External and the change is audited. |
| AC-3 | .5, .6 | **When** the administrator switches Off **then** the mode becomes Internal; if no approved rule is in force a warning says invoices will be blocked. |
| AC-4 | .10, .11, .13, .14 | **Given** a Draft rule with components CGST 9 and SGST 9 for India **when** it is submitted and a different administrator approves it **then** it is Approved and active. **When** its creator tries to approve it **then** it is refused (unless self-approval is allowed). **When** a new version is approved **then** the previous version ends the day before. |
| AC-5 | .12 | **Given** rules that are Draft, Pending, Rejected, deactivated, not yet effective or expired **when** tax is calculated **then** none of them is used. |
| AC-6 | .15 | **Given** a seller in Tamil Nadu and approved rules (within state: CGST 9 + SGST 9; between states: IGST 18) **when** a customer in Tamil Nadu is invoiced 1000.00 **then** the tax lines are CGST 90.00 and SGST 90.00; **when** the customer is in Karnataka **then** IGST 180.00. **When** the customer has a valid GSTIN starting with 29 and a Tamil Nadu billing address **then** the place of supply is Karnataka (IGST). |
| AC-7 | .16 | **Given** rules for another country with a state component and a county component **then** the invoice carries both components. |
| AC-8 | .17 | **Given** a country-wide rule and a state rule that both match **then** the state rule is used. **Given** two different rules of the same specificity **then** the invoice is blocked as ambiguous. |
| AC-9 | .18 | **Given** an approved exemption for a customer in India **when** tax is calculated **then** the result is Exempt with the reason and no component. |
| AC-10 | .20 | **Given** no rule matches the customer's country **when** an invoice is generated **then** it stays Draft with block code NO_MATCHING_RULE; no tax is assumed and the invoice has no number. |
| AC-11 | .30, .31, .33 | **Given** a configured provider **when** the administrator presses Test connection **then** the result and the supported countries and currencies are shown, and no secret appears anywhere in the response. |
| AC-12 | .31, .34 | **Given** external mode and a provider that supports the customer's country **when** an invoice is generated **then** the provider's breakdown is validated, stored with its provider reference and printed on the invoice. **Given** a country the provider does not support **then** the invoice is blocked (PROVIDER_UNSUPPORTED). |
| AC-13 | .35, .36 | **Given** external mode and a provider that fails twice and then answers **then** the call is retried and the invoice is finalized. **Given** a provider that keeps failing **then** the invoice stays Draft (PROVIDER_UNAVAILABLE) and the internal engine is **not** used. |
| AC-14 | .31 (6) | **Given** a response with another currency, another transaction reference, a changed taxable amount, a missing line, a negative tax or sums that do not add up **then** it is rejected (PROVIDER_INVALID_RESPONSE) and the invoice is blocked. |
| AC-15 | .40 | **Given** an issued invoice **when** the switch changes or a rule changes **then** the invoice, its tax lines and its credit notes are unchanged. |
| AC-16 | .41 | **Given** a calculation in progress **when** the switch changes **then** the calculation finishes with the engine it started with and the invoice records that engine and configuration version. |
| AC-17 | .42 | **Given** a Draft invoice **when** the cause is fixed and the administrator presses Retry tax **then** the calculation restarts with the current engine; on success the invoice gets its number, issue date and due date. A Draft is not listed for the customer, cannot be paid and has no document. |
| AC-18 | .44 | **Given** an issued invoice **then** it shows subtotal, each tax component, total, place of supply and per line the tax class and SAC/HSN code. |
| AC-19 | .45 | **Given** an Indian customer **when** they save a tax ID that is not a valid 15-character GSTIN **then** the save is refused with a field error. |
| AC-20 | .46 | **Given** an invoice of 1180.00 (1000.00 + 180.00 tax) **when** 590.00 is refunded **then** a credit note of 500.00 + 90.00 tax with the original components is issued and the invoice's tax lines are unchanged. |
| AC-21 | .47, BR-6 | **Given** any change in this feature **then** an audit entry names the action and the administrator. **Given** a user without `MANAGE_BILLING` **when** they call a tax endpoint **then** the response is 403. |
| AC-22 | .32 | **Given** any tax API response, log line or screen **then** no API key, secret or full base URL path appears. |
