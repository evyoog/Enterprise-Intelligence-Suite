# TC-BIL-098: The webhook set-up is shown per provider and warns when the URL is not public

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-098 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10) |
| Acceptance Criterion | [AC-28](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-12](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 4 (Credentials and webhook configuration are separate per gateway) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A gateway with set-up steps; a second case with `http://localhost:8081/api/webhooks/razorpay`.

## Steps
1. Open View details → Webhook.
2. Read the URL, chips, last event, failure and steps.
3. Repeat with the local address.

## Expected Result
The URL ends with `/webhooks/{provider}`; chips "Webhook secret missing" and "2 rejected webhook(s)" with the reason; the steps are listed; the local address shows "points at this machine".

## Automated coverage
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows capabilities, separate evidence, the webhook set-up and recent failures in the details"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "warns when the webhook URL points at the local machine"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
