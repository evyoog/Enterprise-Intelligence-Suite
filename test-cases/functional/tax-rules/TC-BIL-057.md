# TC-BIL-057: Invoice numbers follow the format EIS/26-27/000123 and restart each financial year

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-057 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.42 (number on finalization); C97 BI-2) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-40](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Billing settings invoice prefix `EIS` (default). The clock is in the financial year 2026-27 (1 April 2026 to 31 March 2027, India time).

## Steps
1. Issue two invoices one after the other.
2. Issue a credit note.
3. Check the financial-year boundary: 1 April 00:00 India time (31 March 18:30 UTC) and one second before it.
4. Check a Draft before and after it is finalized.

## Expected Result
Invoice numbers have the form `EIS/26-27/000123` (prefix / financial year / six-digit sequence) and the second number is the first plus one; credit notes use `EIS-CN/26-27/000007`; with no prefix configured the format still has the `EIS` default. The financial year runs from April to March in India time and the sequence restarts each year. A Draft has no number until its tax calculation succeeds.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/InvoiceNumberAndCreditNoteMathTest.java` — `numbersFollowTheAgreedFormat`, `theFinancialYearRunsFromAprilToMarchInIndiaTime`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `invoiceNumbersFollowTheFinancialYearSequence`, `whenTheRuleIsApprovedRetryTaxIssuesTheInvoiceAndNothingElseChanges`

Not automated: The restart of the sequence in a new financial year is checked by the unit test on the financial-year function, not by issuing invoices across 1 April.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
