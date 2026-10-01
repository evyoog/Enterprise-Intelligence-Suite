# TC-BIL-024: Business profile and invoicing are saved, validated and applied to new invoices

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-024 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.21, .24; C60) |
| Decision | [C60](../../../docs/01-business/roadmap/open-decisions.md#c60) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform admin with MANAGE_BILLING.

## Steps
1. Open Billing settings → Business & invoicing.
2. Enter an invalid GSTIN and save.
3. Enter valid details, prefix "evy" and Net 30; save.
4. Generate a new invoice.
5. Download it.

## Expected Result
Step 2: "GSTIN must be 15 characters…" and nothing is saved. Step 3: the live preview shows the legal name, GSTIN chip, `EVY-<year>-000123` and Net 30; "Settings saved." Step 4: the invoice number starts with `EVY-` and the due date is 30 days after the issue date. Step 5: the document starts with the issuer block (legal name, address, GSTIN, PAN, CIN, contact) and ends with the footer note.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingSettingsServiceTest.java` — `theBusinessProfileIsSavedUpperCasedAndSetsThePrefixAndDueDateOfNewInvoices`, `invalidRegistrationNumbersAndPrefixesAreRefused`, `defaultsKeepTheBehaviourBeforeC60`
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "validates and saves the business profile…"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/controller/InvoiceDocumentsTest.java` — issuer block, payment details and footer on the document

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
