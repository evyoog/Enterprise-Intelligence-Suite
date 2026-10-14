# TC-BIL-072: A provider other than Razorpay works through the same screen and API

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-072 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.2) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-2](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 2 (The page is not Razorpay-only); 14 (New providers need no UI rewrite) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
`FakeGateway` registered as a Spring bean in the test configuration (the pattern for new providers).

## Steps
1. List the gateways and read the summary.
2. Configure FakePay with its three fields, test it, activate it, make it default.
3. Create a payment and send a signed webhook for it.
4. Open the page with FakePay in the list.

## Expected Result
FakePay is IMPLEMENTED, counted in `implemented` (2 with Razorpay), has its own fields and webhook URL, and every step works as for Razorpay. In the screen it is a row with Configure only until it is configured; no provider-specific UI code exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theBackendRoutesByActivationCurrencyMethodAndDefault`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations"

Only the fake provider proves the generic path; no second real provider exists (out of scope, C98).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
