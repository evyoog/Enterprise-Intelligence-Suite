# TC-BIL-009: Checkout shows the four payment options; Pay stays disabled until an option and the terms checkbox are chosen

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-009 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with saved billing details and an OPEN invoice; gateway configured.

## Steps
1. Open `/checkout?invoiceId=<id>&step=payment`.
2. Observe the payment options and the Pay button.
3. Select Card.
4. Tick "I agree to the Terms of Service and the Privacy Policy."

## Expected Result
Step 2: four radio options in order Card, UPI, Other online methods, Pay by invoice; Pay disabled. Step 3: Card checked, Pay still disabled. Step 4: Pay enabled.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows four payment options and keeps Pay disabled until an option and consent are chosen"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
