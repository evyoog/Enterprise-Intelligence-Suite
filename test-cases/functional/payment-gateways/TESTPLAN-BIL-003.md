# TESTPLAN-BIL-003: Payment gateways

| Field | Value |
|---|---|
| Feature ID (required) | FTR-BIL-003 |
| Requirement(s) covered | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98), amended by [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) (administrator-added gateways) |
| Status | Approved with the FRD (product owner, 2026-10-14) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) (TC-BIL-071 to TC-BIL-118, AC-1 to AC-48), including the product owner's 17 acceptance points. TC-BIL-107 to TC-BIL-118 (AC-37 to AC-48) were added by [C99](../../../docs/01-business/roadmap/open-decisions.md#c99): adding, editing and removing a gateway from the screen, templates, encrypted credentials, and the rule that an added gateway is configuration only. C99 **removed the "planned provider" concept**, so TC-BIL-071 and TC-BIL-073 now describe administrator-added gateways. Out of scope (not built, [C98](../../../docs/01-business/roadmap/open-decisions.md#c98), [C99](../../../docs/01-business/roadmap/open-decisions.md#c99)): a payment integration (adapter) for any gateway other than Razorpay, so no added gateway can be tested, activated or take payments; routing by country, amount or cost, automatic fail-over, recurring charges through a provider, saved-card tokens for any provider other than Razorpay.

## Test Strategy
| Level | Approach |
|---|---|
| Unit | `RazorpayGatewayTest` (descriptor, environment and key-prefix rule, status mapping, webhook signature and payload parsing, mocked client), `CredentialCipherTest` (AES-256-GCM, wrong or missing key) |
| Integration | `GatewayManagementServiceTest` (Spring Boot, H2): the generic gateway module exercised with the test-only `FakeGateway` as a second provider (configuration, encryption at rest, rotation, activation, default, routing, methods, per-provider payments, webhooks, idempotency, failure counters) and with a mocked `RazorpayClient`, and, since C99, adding a gateway (validation, duplicates, encryption, templates), editing its definition, removing it and routing that never reaches it; `PaymentGatewayAuthorizationTest` (MockMvc): `MANAGE_BILLING`, no secret in a response, adding, configuring and removing a gateway over HTTP, public signed webhooks; `BillingServiceTest` (existing Razorpay flows) |
| Frontend | Vitest, Testing Library and jest-axe: `AdminPaymentGatewayPage.test.tsx` (including the Add, Edit and Remove dialogs), `AdminBillingSettingsPage.test.tsx`, `CheckoutPage.test.tsx`, API mocked |
| Manual | TC-BIL-103 to TC-BIL-105 are Not Run: a real Razorpay connection test, a real signed webhook delivery, and the V038 migration on a real PostgreSQL database; TC-BIL-106 is automated only at the cipher level; TC-BIL-117 (no encryption key: add or save with values refused) and TC-BIL-118 (migration V039 on a real database) are manual and Not Run. Parts of other cases listed as "Not covered by an automated test" are manual too |

Test pattern for new providers: `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/FakeGateway.java` (see [09-integrations/payment-gateways.md](../../../docs/09-integrations/payment-gateways.md#how-to-add-a-new-provider)).

## Environment for the manual cases
Razorpay Test keys; a public backend URL set in `BACKEND_URL`; `PAYMENT_GATEWAY_ENCRYPTION_KEY` (from `openssl rand -base64 32`); the webhook registered in the Razorpay Dashboard for payment.captured, payment.failed and refund.processed; PostgreSQL 16 with the schema at V037 (V038 for TC-BIL-105; V038 and then V039 for TC-BIL-118).

## Test Cases in This Plan
| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-BIL-071](TC-BIL-071.md) | AC-1 The page lists every registered gateway (nothing as planned) with an overview and only valid actions | P0 | Yes |
| [TC-BIL-072](TC-BIL-072.md) | AC-2 A provider other than Razorpay works through the same screen and API | P0 | Yes |
| [TC-BIL-073](TC-BIL-073.md) | AC-3 A gateway without a payment integration is never operational | P0 | Partly |
| [TC-BIL-074](TC-BIL-074.md) | AC-4 Gateways are configured independently | P0 | Partly |
| [TC-BIL-075](TC-BIL-075.md) | AC-5 Credentials and webhook configuration are per gateway | P0 | Yes |
| [TC-BIL-076](TC-BIL-076.md) | AC-6 Credentials are write-only and masked | P0 | Yes |
| [TC-BIL-077](TC-BIL-077.md) | AC-7 Credentials are encrypted at rest with a master key from the environment, and refused without it | P0 | Partly |
| [TC-BIL-078](TC-BIL-078.md) | AC-8 Configured, Connected, Active and Default are four separate statuses | P0 | Yes |
| [TC-BIL-079](TC-BIL-079.md) | AC-9 A connection test calls the real provider and shows what it answered | P0 | Partly |
| [TC-BIL-080](TC-BIL-080.md) | AC-10 Activation is blocked while the configuration is incomplete or the test fails | P0 | Yes |
| [TC-BIL-081](TC-BIL-081.md) | AC-11 Several gateways can be active at once | P0 | Yes |
| [TC-BIL-082](TC-BIL-082.md) | AC-12 A default gateway can be selected, and it must be active | P0 | Yes |
| [TC-BIL-083](TC-BIL-083.md) | AC-13 Changing the credentials of an active gateway tests first and keeps the old ones on failure | P0 | Yes |
| [TC-BIL-084](TC-BIL-084.md) | AC-14 The backend routes each new payment by activation, currency, method and default | P0 | Yes |
| [TC-BIL-085](TC-BIL-085.md) | AC-15 With no eligible gateway the payment is refused clearly and nothing is created | P0 | Yes |
| [TC-BIL-086](TC-BIL-086.md) | AC-16 A payment keeps the provider that created it | P0 | Yes |
| [TC-BIL-087](TC-BIL-087.md) | AC-17 Webhooks are accepted only with a valid provider signature | P0 | Yes |
| [TC-BIL-088](TC-BIL-088.md) | AC-18 Webhook events are idempotent per provider and event id | P0 | Yes |
| [TC-BIL-089](TC-BIL-089.md) | AC-19 Webhook failures are one counter, and the last event is shown | P1 | Yes |
| [TC-BIL-090](TC-BIL-090.md) | AC-20 A deactivated but configured provider still has its webhooks accepted | P1 | Partly |
| [TC-BIL-091](TC-BIL-091.md) | AC-21 Payment methods are switched per gateway and unsupported ones are never offered | P1 | Yes |
| [TC-BIL-092](TC-BIL-092.md) | AC-22 Razorpay's environment must match its key prefix | P1 | Yes |
| [TC-BIL-093](TC-BIL-093.md) | AC-23 Existing Razorpay installations and behaviour keep working | P0 | Partly |
| [TC-BIL-094](TC-BIL-094.md) | AC-24 Provider failures never create a wrong status | P0 | Yes |
| [TC-BIL-095](TC-BIL-095.md) | AC-25 Existing routes and permissions stay compatible, and only MANAGE_BILLING reaches the gateway API | P0 | Partly |
| [TC-BIL-096](TC-BIL-096.md) | AC-26 Every administrator change is audited without values | P1 | Partly |
| [TC-BIL-097](TC-BIL-097.md) | AC-27 Evidence flags come from real records, not from the connection test | P1 | Yes |
| [TC-BIL-098](TC-BIL-098.md) | AC-28 The webhook set-up is shown per provider and warns when the URL is not public | P1 | Yes |
| [TC-BIL-099](TC-BIL-099.md) | AC-29 The Configure dialog is built from the provider's fields and keeps secrets write-only | P0 | Yes |
| [TC-BIL-100](TC-BIL-100.md) | AC-30 The checkout shows the provider the backend chose, and an unsupported provider charges nothing | P0 | Yes |
| [TC-BIL-101](TC-BIL-101.md) | AC-31 Billing settings embeds the same gateway list | P1 | Yes |
| [TC-BIL-102](TC-BIL-102.md) | AC-32 The gateway screen is accessible and handles load errors | P1 | Partly |
| [TC-BIL-103](TC-BIL-103.md) | AC-33 A real Razorpay connection test works against the Razorpay Test environment | P0 | No |
| [TC-BIL-104](TC-BIL-104.md) | AC-34 A real signed Razorpay webhook is accepted once | P0 | No |
| [TC-BIL-105](TC-BIL-105.md) | AC-35 Migration V038 upgrades an existing installation without interrupting Razorpay | P0 | No |
| [TC-BIL-106](TC-BIL-106.md) | AC-36 A changed or lost master key leaves stored credentials unreadable and says so | P1 | Partly |
| [TC-BIL-107](TC-BIL-107.md) | AC-37 An administrator adds a gateway from scratch and it is listed as having no payment integration | P0 | Yes |
| [TC-BIL-108](TC-BIL-108.md) | AC-38 An administrator adds a gateway from a template (Stripe) with credential values | P1 | Yes |
| [TC-BIL-109](TC-BIL-109.md) | AC-39 Credentials of an added gateway are stored encrypted and never returned | P0 | Yes |
| [TC-BIL-110](TC-BIL-110.md) | AC-40 Adding a gateway validates the definition and saves nothing when it is wrong | P0 | Partly |
| [TC-BIL-111](TC-BIL-111.md) | AC-41 A code that is reserved, malformed, a built-in provider or already used is refused | P1 | Partly |
| [TC-BIL-112](TC-BIL-112.md) | AC-42 Editing a definition keeps the other values, drops the values of removed fields and cannot touch a built-in provider | P1 | Partly |
| [TC-BIL-113](TC-BIL-113.md) | AC-43 An administrator removes an added gateway and its stored credentials; a built-in provider cannot be removed | P1 | Partly |
| [TC-BIL-114](TC-BIL-114.md) | AC-44 A configuration-only gateway cannot be tested, activated or routed to, and is counted honestly | P0 | Partly |
| [TC-BIL-115](TC-BIL-115.md) | AC-45 Only MANAGE_BILLING can add, edit, remove or read the templates of gateways | P0 | Partly |
| [TC-BIL-116](TC-BIL-116.md) | AC-46 The Add, Edit and Remove dialogs guide the administrator and show the server's refusal | P1 | Yes |
| [TC-BIL-117](TC-BIL-117.md) | AC-47 Without the encryption key an add or save with credential values is refused clearly (manual) | P0 | No |
| [TC-BIL-118](TC-BIL-118.md) | AC-48 Migration V039 adds the columns for administrator-added gateways without changing existing data (manual) | P0 | No |

## Notes
- The earlier C97 gateway test cases [TC-BIL-069](../billing-payments/TC-BIL-069.md) (list and make active) and [TC-BIL-070](../billing-payments/TC-BIL-070.md) cite the test class `PaymentGatewayAndPoTest`, which C98 replaced (`PurchaseOrderNumberTest` keeps the purchase-order test; the gateway tests are now in `GatewayManagementServiceTest`). TC-BIL-069 is superseded by AC-1, AC-10 to AC-12 here.
- C99 (2026-10-14) renamed or removed three automated tests that the earlier cases cited: `theRegistryListsRazorpayAsOperationalAndTheOthersAsPlannedWithNoActions` is now `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned`; `aPlannedProviderCannotBeConfiguredTestedOrActivated` (PaymentGatewayAuthorizationTest) and the frontend test "lists a provider without an integration as planned, never operational" no longer exist and their cases (TC-BIL-071, TC-BIL-073) were rewritten. Stripe, PayPal, PayU and Cashfree are templates for the Add form, not "planned providers".
- The backend results come from the Surefire reports in `backend/target/surefire-reports` dated 2026-10-14; re-run `cd backend && mvn -B verify` before a release.
