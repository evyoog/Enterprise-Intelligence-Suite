# TC-BIL-093: Existing Razorpay installations and behaviour keep working

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-093 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.14) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-5, BR-17](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 13 (The existing Razorpay flow still works); 17 (Existing routes and permissions stay compatible) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Partly |

## Preconditions
Razorpay credentials in the test environment; no stored rows (the legacy state).

## Steps
1. Read the gateway list with no rows stored.
2. Pay an invoice with a verified signature and with an invalid one.
3. Deliver a Razorpay webhook twice.
4. Try to refund more than captured.
5. Read the Razorpay status endpoint.
6. Pay in the checkout with a saved card and with netbanking.

## Expected Result
Razorpay is configured (source ENVIRONMENT), active and default; a verified payment is CAPTURED, an invalid signature changes nothing; the webhook is processed once; the over-refund is refused; the status endpoint shows the masked Key ID and no secret; the checkout flows work.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `payingAnInvoiceWithAVerifiedCapturedPaymentMarksItPaid`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `anInvalidSignatureChangesNothing`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `aWebhookEventIsProcessedAtMostOnce`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `refundingMoreThanCapturedIsRefused`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `gatewayStatusMasksTheKeyIdAndNeverReturnsSecrets`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `theDetailShowsWhetherEachCredentialIsSetAndNeverItsValue`
- `frontend/src/pages/CheckoutPage.test.tsx` — "pays by netbanking with that method preselected in Razorpay"
- `frontend/src/pages/CheckoutPage.test.tsx` — "pays with the default saved card; an expired card cannot be chosen"
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows a failed result with retry options when the customer closes Razorpay"

Not covered by an automated test: the earlier `POST /admin/billing/gateway/test` and the alias `PUT /admin/billing/gateways/active`; a stored value overriding a secrets-file value for Razorpay; the saved-card (token) endpoints against a real Razorpay.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
