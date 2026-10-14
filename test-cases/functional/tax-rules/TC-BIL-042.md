# TC-BIL-042: Tax classes, product classification and supplier registrations

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-042 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.19, .37) |
| Acceptance Criterion | None specific (the requirement has no acceptance criterion of its own) |
| Business rules | [BR-12](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-32](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator; at least one product; the business profile (Billing settings) has a GSTIN.

## Steps
1. On the Classification tab create the class SAAS with SAC code 998439, then edit its name (the code is locked).
2. Assign SAAS to a product and clear it again.
3. On the Registrations tab add a registration (country, state, tax type, number, dates), edit it and delete it.
4. Read the business profile's GSTIN row.

## Expected Result
Classes are created, updated and assigned to products with a toast and audit entries (`TAX_CLASS_CREATED`, `TAX_CLASS_UPDATED`, `PRODUCT_TAX_CLASS_CHANGED`, `TAX_REGISTRATION_CREATED`, `TAX_REGISTRATION_UPDATED`, `TAX_REGISTRATION_DELETED`). A class code is letters, digits, `-`, `_` and never changes. The GSTIN of the business profile is listed read-only with a "From business profile" chip and cannot be edited or deleted here. The invoice line later carries the class and SAC/HSN code, and the external engine sends them to the provider.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `taxClassesAreCreatedUpdatedAndAssignedToProducts`, `registrationsAreKeptAndTheBusinessProfileGstinIsListedReadOnly`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "assigns a tax class to a product", "creates a tax class", "lists registrations and marks the business profile GSTIN as read-only"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
