# TC-BIL-066: Pay by invoice is only for organizations the administrator enables

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-066 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.28; BR-18) |
| Acceptance Criterion | [AC-42](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An individual customer; an organization not enabled; an administrator with `MANAGE_BILLING`.

## Steps
1. As the individual, choose Pay by invoice.
2. As the organization, read the checkout summary and choose Pay by invoice.
3. As the administrator, enable the organization; read the summary again; find it by name search and in the enabled list.
4. Disable it again.
5. Switch the global Pay by invoice switch off.
6. In Billing settings, list the enabled organizations, find another by name and switch it on.

## Expected Result
Steps 1 and 2: refused (403), `payByInvoiceAllowed` false. Step 3: allowed, listed and found. Step 4: refused again. Step 5: refused for everyone. Step 6: the switch saves at once and the organization moves to the enabled list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/CheckoutOfflinePaymentTest.java` — `payByInvoiceIsRefusedForAnIndividualAndForAnOrganizationThePlatformAdminHasNotEnabled`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingSettingsServiceTest.java` — `switchingMethodsOffChangesTheCheckoutAndRefusesPayByInvoice`
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "lists the organizations allowed to pay by invoice and enables one found by name (C97 BI-4)"

Not covered by an automated test: the audit entries `ORG_PAY_BY_INVOICE_ENABLED` / `_DISABLED` and the migration backfill of organizations that already had OFFLINE invoices.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend test passed in a single-file run on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
