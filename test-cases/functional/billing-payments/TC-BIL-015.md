# TC-BIL-015: An admin records the full offline payment and the invoice becomes PAID

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-015 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform admin with `MANAGE_BILLING`; an OPEN invoice with route OFFLINE.

## Steps
1. Admin → Billing → Invoices & payments: click **Record offline payment** on the invoice.
2. Enter the full amount, today's date, method NEFT/RTGS and a reference; click **Record payment**.
3. Confirm "Mark invoice <number> as paid?".

## Expected Result
The button appears only on OPEN OFFLINE-route invoices. After confirmation the invoice is PAID, a CAPTURED payment with method type OFFLINE and the reference exists, the receipt is downloadable, an audit entry names the admin, and a success toast is shown. Refund and Reconcile are not offered for the offline payment.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingPage.test.tsx` — "offers Record offline payment only on OPEN invoices…", "records an offline payment after validation and a confirmation step", "hides gateway refund and reconcile for offline payments"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/CheckoutOfflinePaymentTest.java` — `recordingTheFullAmountMarksTheInvoicePaidWithAnOfflinePayment`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
