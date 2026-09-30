# TC-BIL-001: A zero-amount subscription generates no invoice; a paid one generates an OPEN invoice in minor units

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-001 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer and an ACTIVE subscription to a product.

## Steps
1. Act: generate an invoice for a subscription to a $0 product.
2. Observe the result.
3. Act: generate an invoice for a subscription to a $19.99 product.
4. Observe the invoice.

## Expected Result
Step 2: no invoice is created (null). Step 4: an OPEN invoice with total 1999 (minor units), currency USD, and an invoice number starting "INV-".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `generatingAnInvoiceForAZeroAmountPlanDoesNothing`, `generatingAnInvoiceForAPaidPlanCreatesAnOpenInvoiceInMinorUnits`

## Actual Result
The automated tests passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
