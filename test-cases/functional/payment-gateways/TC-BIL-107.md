# TC-BIL-107: AC-37 An administrator adds a gateway from scratch and it is listed as having no payment integration

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-107 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-37](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-20, BR-21, BR-23](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 1 (The page shows the registered providers); 14 (New providers need no UI rewrite) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `MANAGE_BILLING`; Razorpay active and default; no gateway with the code ACME.

## Steps
1. Open `/admin/billing/payment-gateway` and choose **Add payment gateway**.
2. Type the name "Acme Pay" and a credential label "API secret key" (leave Start from, code, currencies, methods and the credential value as they are).
3. Check that the code reads ACME_PAY and the credential name apiSecretKey, then choose **Add gateway**.
4. Read the list, the overview and the response of `POST /admin/billing/gateways`.

## Expected Result
201 with the gateway details. The dialog closes and the toast says "Acme Pay was added.". The list has a new card "Acme Pay" with environment Test, currency INR, Cards, the chips Not configured, **No payment integration** and Inactive, the line "Added here: the credentials are stored encrypted, but EIS has no payment integration for this gateway, so it cannot be tested, activated or used to take payments.", and only the buttons Configure, View details, Edit and Remove. Registered gateways goes up by one and Need configuration goes up by one; Razorpay is unchanged (still active and default).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAddedGatewayCanBeConfiguredLaterRotatedEditedAndRemoved` (adds a gateway with no values: Not configured)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAdministratorCanAddAGatewayAndItsCredentialsAreStoredEncryptedAndNeverReturned` (code upper-cased, CONFIGURATION_ONLY, inactive, actions, registered and implemented counts)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (201 and `$.gateway.implementation` CONFIGURATION_ONLY over HTTP)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from scratch: derives the code and credential names and keeps the Add button off until the form is valid"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "labels a gateway added by an administrator as having no payment integration and offers only what works"

Not covered by an automated test: that the overview tiles change by exactly one, and the "Added here: …" line wording in a real browser (the frontend test asserts the chip and the line with the API mocked).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
