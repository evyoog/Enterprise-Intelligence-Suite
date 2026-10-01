# TC-BIL-013: Online options are disabled when the gateway is not configured; Pay by invoice stays available

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-013 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-24](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
No Razorpay credentials in `config/secrets.env`.

## Steps
1. Open the checkout payment step.
2. Try to select Card.

## Expected Result
Banner "Online payments are not available yet."; Card, UPI and Other online methods are `aria-disabled` and cannot be selected; Pay by invoice can be selected.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "disables online options when the gateway is not configured but keeps Pay by invoice"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
