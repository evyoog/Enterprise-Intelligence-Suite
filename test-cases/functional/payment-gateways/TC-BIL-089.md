# TC-BIL-089: Webhook failures are one counter, and the last event is shown

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-089 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-13](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 11 (Webhooks are signature-verified) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay configured.

## Steps
1. Send three bad deliveries (wrong signature, no signature, garbage body).
2. Read the webhook status.
3. Send one valid event.
4. Read the status and the recent events; open the Webhook and Activity tabs.

## Expected Result
`failureCount` 3 with a reason and status NOT_SEEN; after the valid event status VERIFIED, last event type set, one recent event (no rows for the failures). The tabs show "2 rejected webhook(s)" with the last reason, and the recent events.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows capabilities, separate evidence, the webhook set-up and recent failures in the details"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
