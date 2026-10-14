# TC-BIL-073: A gateway without a payment integration is never operational

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-073 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.3) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-1, BR-23](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 15 (Unsupported providers are never operational) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98), amended by [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) (the "planned provider" concept was replaced by administrator-added, configuration-only gateways) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The default registry plus one gateway an administrator added (code HTTPPAY, no adapter); an administrator.

## Steps
1. Call test and activate for HTTPPAY, and then for STRIPE (never added).
2. Send `POST /webhooks/stripe` (a code nobody added) and `POST /webhooks/httppay`.
3. Open the page and look at the HTTPPAY card and its View details.

## Expected Result
Test and activate are 409 `INVALID_STATE` for the added gateway ("… has no payment integration in EIS, so it can store credentials but cannot be tested, activated or used for payments."); a code that is neither a built-in provider nor an added gateway is 404, also for the webhook; a webhook to the added gateway is 404 too. The card shows "No payment integration" and no Test, Activate, Deactivate or Make default button; its details dialog says nothing can be tested, activated or used for payments. Nothing is shown as "planned": Stripe is not listed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAdministratorCanAddAGatewayAndItsCredentialsAreStoredEncryptedAndNeverReturned` (test and activate on an added gateway are refused with "no payment integration")
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned` (an unknown code is 404; nothing is listed as planned)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (test and activate on an added gateway are 409 over HTTP; an unknown code is 404)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `webhooksArePublicButNeedAValidSignatureAndAKnownProvider` (`/webhooks/stripe`, which nobody added, is 404)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aWebhookOfAnUnknownOrPlannedProviderIsRefusedAndEventIdsAreUniquePerProviderOnly` (an unknown provider is 404)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "labels a gateway added by an administrator as having no payment integration and offers only what works"

Not covered by an automated test: the 409 of deactivate, default and methods for an added gateway, and the 404 of a webhook sent to an added gateway (see [TC-BIL-114](TC-BIL-114.md)). The method name `aWebhookOfAnUnknownOrPlannedProviderIsRefusedAndEventIdsAreUniquePerProviderOnly` still says "Planned" but now tests an unknown provider.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
