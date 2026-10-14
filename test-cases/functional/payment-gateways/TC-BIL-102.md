# TC-BIL-102: The gateway screen is accessible and handles load errors

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-102 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.1) |
| Acceptance Criterion | [AC-32](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-2](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 1 (The page shows the registered providers) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The page rendered with a mocked API.

## Steps
1. Render the page and run axe.
2. Make the first list call fail, then press Retry.

## Expected Result
No axe violations on the page; the error alert with Retry appears; Retry shows the list. Dialogs, keyboard use and both languages are checked manually.

## Automated coverage
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows a load error with Retry"

Not covered by an automated test: axe on the Configure and Details dialogs, keyboard-only use, dark theme, phone width, and Spanish text (`es.json` has the keys; no test compares them).

## Actual Result
The automated tests listed above passed (frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
