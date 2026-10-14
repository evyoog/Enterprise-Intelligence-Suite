# TC-BIL-069: The payment gateways list shows Razorpay, never a secret, and a configured gateway can be made active

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-069 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.31; BR-21) |
| Acceptance Criterion | [AC-45](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `MANAGE_BILLING`.

## Steps
1. Open Billing settings, Payment gateways.
2. Make Razorpay active; try an unknown code.
3. Open a checkout whose active gateway is not Razorpay.

## Expected Result
Razorpay is listed and active with Cards, UPI, Netbanking, Wallets and no secret; an unknown code is 404; the footer names a non-Razorpay gateway instead of showing the Razorpay logo.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PaymentGatewayAndPoTest.java` — `razorpayIsTheFirstGatewayAndTheListNeverCarriesASecret`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PaymentGatewayAndPoTest.java` — `aConfiguredGatewayCanBeMadeActiveAndAnUnknownOneIsRefused`
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "lists payment gateways, marks the active one and never shows a key (C97 BI-8)"
- `frontend/src/pages/CheckoutPage.test.tsx` — "names the active gateway in the footer when it is not Razorpay (C97 BI-8)"

Not covered by an automated test: making an unconfigured gateway active (400) and the audit entry `BILLING_ACTIVE_GATEWAY_CHANGED`; only Razorpay exists today.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
