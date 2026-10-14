# TC-BIL-044: The preview calculator shows components or the block reason and creates nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-044 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.21) |
| Acceptance Criterion | None specific (the requirement has no acceptance criterion of its own) |
| Business rules | [BR-18](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Approved India rules; External Tax Engine On or Off.

## Steps
1. On the Rules tab enter 1000.00 INR, country India, state Tamil Nadu, customer type Individual and press Calculate.
2. Repeat with a country that has no rule.
3. Check the invoices list afterwards.

## Expected Result
Step 1: the place of supply, one row per component with rate and amount, total tax and total. Step 2: a warning with the block reason. No invoice, calculation or audit record is created. The preview always uses the internal rules.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `thePreviewShowsTheComponentsOrTheBlockReasonAndCreatesNothing`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "previews a calculation and shows each component", "shows why a preview is blocked instead of a zero"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
