# TC-BIL-065: An issued invoice has the number format EIS/YY-YY/NNNNNN and is due in 15 days

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-065 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.26; BR-16) |
| Acceptance Criterion | [AC-41](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A Draft invoice whose tax rule is approved afterwards.

## Steps
1. Retry tax so the invoice is issued.
2. Read its number and dates.

## Expected Result
The number matches `EIS/YY-YY/NNNNNN` (the test uses the pattern `EIS/\d{2}-\d{2}/\d{6}`) and the due date is 15 days after the issue date.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `whenTheRuleIsApprovedRetryTaxIssuesTheInvoiceAndNothingElseChanges`

## Actual Result
The automated test passed on 2026-10-13 (`mvn -B -o test -Dtest=InvoiceTaxIntegrationTest`). The test belongs to the tax work.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
