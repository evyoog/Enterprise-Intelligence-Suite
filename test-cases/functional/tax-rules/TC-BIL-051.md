# TC-BIL-051: Switching the engine affects only new calculations; issued invoices and credit notes never change

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-051 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.40, .43) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-5](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-10](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An invoice issued under the internal engine (CGST 90.00 + SGST 90.00 on 1000.00) with a partial credit note.

## Steps
1. Switch External On (validated provider) and generate a new invoice.
2. Switch Off again, change or delete the internal rules (for example add a rule with 28%), and generate another invoice.
3. Open the first invoice, its tax lines and its credit notes.

## Expected Result
The new invoices use the engine in force at their creation and record the engine and configuration version. The first invoice, its tax lines and its credit notes are unchanged, even after every rule is removed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `switchingEnginesChangesOnlyFutureInvoicesAndNeverAnIssuedOne`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `anIssuedInvoiceNeverChangesWhenTheRulesChangeAfterwards`, `aCreditNoteUsesTheOriginalRatesEvenAfterTheEngineChanged`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
