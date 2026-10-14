# TC-BIL-068: Terms, Privacy and Refund links are set in Billing settings, linked at checkout and printed on invoices

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-068 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.30; BR-20) |
| Acceptance Criterion | [AC-44](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `MANAGE_BILLING`; a checkout with and without the pages set.

## Steps
1. Save the three URLs in Billing settings; save a URL without http(s).
2. Open the checkout.
3. Download an invoice.

## Expected Result
A bad URL is refused. The consent sentence links to the three pages (plain text when not set). The invoice prints Terms, Privacy and Refunds lines.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "saves the Terms, Privacy and Refund page links and rejects a bad one (C97 BI-7)"
- `frontend/src/pages/CheckoutPage.test.tsx` — "links the consent sentence to the Terms, Refund and Privacy pages set in Billing settings (C97 BI-7)"
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the consent sentence without links when no page is set"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/controller/InvoiceDocumentsTest.java` — `anOfflineInvoicePrintsTheIssuerThePaymentDetailsAndTheFooter`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
