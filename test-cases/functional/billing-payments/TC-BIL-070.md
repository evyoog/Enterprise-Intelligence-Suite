# TC-BIL-070: An optional purchase-order number can be added and cleared until the invoice is paid

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-070 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.32; BR-22) |
| Acceptance Criterion | [AC-46](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with an OPEN invoice; another customer.

## Steps
1. Save " PO-2026/17 ".
2. Read the checkout summary.
3. Save blank.
4. Save "PO-9", pay the invoice, then try to change it.
5. As the other customer, set a number on the invoice.
6. On the checkout, type "!!" in the field.

## Expected Result
Step 1: stored trimmed. Step 2: shown. Step 3: cleared. Step 4: refused (409). Step 5: 404. Step 6: the field says "Use up to 50 letters, digits, spaces or . / _ -" and no request is made. The number is printed on the invoice.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PaymentGatewayAndPoTest.java` — `aPurchaseOrderNumberCanBeAddedClearedAndOnlyBeforePayment`
- `frontend/src/pages/CheckoutPage.test.tsx` — "saves an optional purchase-order number when the field loses focus, and refuses a bad one (C97 BI-9)"

Not covered by an automated test: the number being printed on the invoice document (`InvoiceDocuments`); check a downloaded invoice manually.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
