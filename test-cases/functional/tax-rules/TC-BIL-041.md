# TC-BIL-041: An approved exemption makes the result an explicit Exempt with the reason

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-041 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.18) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-19](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-22](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Approved India rules. A customer in India. Administrators Admin 1 and Admin 2.

## Steps
1. As Admin 1 create an exemption for the customer: country India, reason "SEZ unit", certificate "SEZ-42", effective from yesterday.
2. Calculate tax for the customer while the exemption is Pending approval.
3. As Admin 2 approve the exemption and calculate again.
4. Calculate for a different customer, and calculate after the exemption has ended or been revoked.

## Expected Result
Step 2: normal tax is charged (pending exemptions are ignored). Step 3: the result is Exempt with zero tax, one line with treatment Exempt whose note contains "SEZ unit" and "SEZ-42", and no component is charged. Step 4: other customers, other countries, expired and revoked exemptions pay normal tax. The approver must be another administrator (BR-22).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `anApprovedExemptionMakesTheResultAnExplicitExempt`, `anExemptionOnlyAppliesToItsOwnerAndCountry`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `anExemptionIsApprovedByAnotherAdministrator`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "lists exemptions and approves a pending one"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
