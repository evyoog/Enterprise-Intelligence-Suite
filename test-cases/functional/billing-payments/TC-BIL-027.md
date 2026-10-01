# TC-BIL-027: The Razorpay credentials view never takes or shows a key (BR-SEC-001)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-027 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.21, .24; C60) |
| Decision | [C60](../../../docs/01-business/roadmap/open-decisions.md#c60) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay keys not set, then set, in `config/secrets.env`.

## Steps
1. Open Billing settings → Razorpay gateway (and Admin → Payment gateway).
2. Set the keys in the secrets file, restart, reopen and click Test connection.

## Expected Result
Step 1: Not configured; Key ID, Key secret and Webhook secret each "Missing"; Test connection disabled; the five steps list `RAZORPAY_KEY_ID=`, `RAZORPAY_KEY_SECRET=`, `RAZORPAY_WEBHOOK_SECRET=` with Copy; no text input exists. Step 2: the key ID is masked (`rzp_test_••••••7890`), the secrets show "Present", and Test connection reports "Connected."

## Automated coverage
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "shows the Razorpay credential status without any key input (BR-SEC-001)"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — full file

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
