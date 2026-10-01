# TC-BIL-018: Checkout, the Record offline payment dialog and the Billing settings form pass the axe test

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-018 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-30](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Run axe on the checkout billing-details and payment steps, the Record offline payment dialog and the Billing settings page.
2. Use arrow keys on the payment options.

## Expected Result
No axe violations; the payment options are a `radiogroup` operable with arrow keys and Space/Enter; the stepper marks the current step with `aria-current="step"`.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — the "has no detectable accessibility violations" tests and the order-submitted axe check
- `frontend/src/pages/admin/AdminBillingPage.test.tsx` — dialog axe check
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "shows the empty state…" axe check

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
