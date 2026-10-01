# TC-BIL-022: Complete order never starts a payment

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-022 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18, .22, .23; C59) |
| Acceptance Criterion | [AC-34](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Payment step.

## Steps
1. Look at the footer bar before and during a payment.

## Expected Result
Step back is on the left; Complete order is disabled; the confirmed result moves to Complete by itself; only Pay / Generate invoice starts a payment.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows five method tiles…" (Complete order disabled)

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
