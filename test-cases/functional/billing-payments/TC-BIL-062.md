# TC-BIL-062: A tax-blocked Draft cannot be paid, so its purchase stays pending

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-062 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.25; BR-15) |
| Acceptance Criterion | [AC-38](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer whose invoice is a Draft because no tax rule matches.

## Steps
1. As the customer, list invoices and open the checkout for the Draft.
2. Try to choose Pay by invoice on it.

## Expected Result
The Draft is not in the customer's list; the checkout summary shows tax pending and no invoice number; choosing Pay by invoice is refused.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `aDraftIsHiddenFromTheCustomerCannotBePaidAndShowsAsTaxPendingAtCheckout`

Not covered by an automated test: the subscription remaining PENDING_SUBSCRIPTION (follows because activation happens only on payment or Pay by invoice; not asserted).

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
