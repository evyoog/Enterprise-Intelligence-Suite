# TC-BIL-099: The Configure dialog is built from the provider's fields and keeps secrets write-only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-099 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.4, .5) |
| Acceptance Criterion | [AC-29](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-3, BR-4](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked); 14 (New providers need no UI rewrite) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay active; FakePay with fields of its own.

## Steps
1. Open Configure Razorpay.
2. Type only a new webhook secret and Save.
3. Save with the server refusing.
4. Open Configure when no encryption key is available.

## Expected Result
The request is `{environment: TEST, fields: {webhookSecret}}` only; the success toast "Razorpay configuration saved."; the refusal shows "The previous configuration is unchanged" in the dialog; the no-key dialog shows the warning and Save is disabled.

## Automated coverage
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "builds the configuration form from the provider fields, keeps secrets write-only and sends only what was typed"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows a refused save and says when the server cannot store credentials"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "refuses to save when the server has no encryption key"

## Actual Result
The automated tests listed above passed (frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
