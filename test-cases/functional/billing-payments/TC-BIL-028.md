# TC-BIL-028: Billing settings and the corporate page header pass the axe test

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-028 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.21, .24; C60) |
| Decision | [C60](../../../docs/01-business/roadmap/open-decisions.md#c60) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Run axe on Billing settings (Business and Offline tabs) and on the Payment gateway screen.

## Expected Result
No axe violations; overview tiles are buttons with `aria-pressed`; switches have accessible names; the save bar status is announced.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — axe checks
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — axe check

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
