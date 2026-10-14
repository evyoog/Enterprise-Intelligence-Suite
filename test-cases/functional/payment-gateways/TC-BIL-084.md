# TC-BIL-084: The backend routes each new payment by activation, currency, method and default

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-084 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.12) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-10](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 10 (The backend routes each payment) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay (all methods, USD EUR GBP INR) and FakePay (INR and USD; card and UPI), both active.

## Steps
1. Route a USD payment.
2. Make FakePay default and route INR card, EUR card and INR wallet.
3. Deactivate Razorpay and route INR UPI.
4. Switch Razorpay methods off and read the offered methods.

## Expected Result
Razorpay is chosen first while it is default; after the change FakePay for INR card; Razorpay for EUR (FakePay lacks it) and for wallet (FakePay lacks it); FakePay for INR UPI when Razorpay is inactive; offered methods exclude those switched off.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theBackendRoutesByActivationCurrencyMethodAndDefault`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `paymentMethodsCanBeSwitchedOffButOnlyOnesTheProviderSupports`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
