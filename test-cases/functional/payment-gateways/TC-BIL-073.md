# TC-BIL-073: A planned provider is never operational

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-073 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.3) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-1](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 15 (Unsupported providers are never operational) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The default registry; an administrator.

## Steps
1. Call test, activate and configuration for STRIPE.
2. Send `POST /webhooks/stripe`.
3. Open the page and View details on Stripe.

## Expected Result
409 `INVALID_STATE` for each admin action, 404 for the webhook, an unknown code is 404. The page shows "Integration planned", only View details, and a details dialog that says nothing can be configured or used.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsRazorpayAsOperationalAndTheOthersAsPlannedWithNoActions`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `aPlannedProviderCannotBeConfiguredTestedOrActivated`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `webhooksArePublicButNeedAValidSignatureAndAKnownProvider`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aWebhookOfAnUnknownOrPlannedProviderIsRefusedAndEventIdsAreUniquePerProviderOnly`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "lists a provider without an integration as planned, never operational"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
