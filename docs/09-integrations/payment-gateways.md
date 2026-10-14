# Integration — Payment gateways (provider-independent)

| Field | Value |
|---|---|
| Purpose | Collect invoice payments through any number of payment providers, configured and activated by a platform administrator, with the backend choosing the provider for each payment |
| Decision | [C98](../01-business/roadmap/open-decisions.md#c98) (extends [C46](../01-business/roadmap/open-decisions.md#c46) Razorpay and [C97](../01-business/roadmap/open-decisions.md#c97) BI-8) |
| Requirement | [REQ-BIL-003](../02-requirements/FRD/payment-gateways/requirement.md); rules [business-rules.md](../02-requirements/FRD/payment-gateways/business-rules.md); API [payment-gateways.md](../06-api/api-requirements/payment-gateways.md); screen [admin-payment-gateway.md](../05-ui/screen-requirements/admin-payment-gateway.md) |
| Status | **Razorpay is the only real integration** ([razorpay.md](razorpay.md)). Stripe, PayPal, PayU and Cashfree are listed as planned and are not operational |

## How it is put together
| Piece | Where | Role |
|---|---|---|
| `PaymentGateway` interface | `backend/src/main/java/com/vyoog/eisplatform/modules/billing/service/gateway/PaymentGateway.java` | One working integration with one provider: descriptor, connection test, create order, verify payment, fetch payment, refund, verify webhook, parse webhook. Adapters are stateless: credentials and environment arrive in a `GatewayContext` |
| `GatewayDescriptor` and `ConfigField` | same package | What the registry knows about a provider: code, name, description, logo key, countries, currencies, methods, transaction types, environments, webhook / refund / recurring support, configuration fields, set-up steps, webhook events, `IMPLEMENTED` or `PLANNED` |
| `GatewayRegistry` | same package | Collects every `PaymentGateway` Spring bean plus the planned entries; an adapter replaces the planned entry with the same code |
| `PlannedProviders` | same package | Providers that are listed with what they will need but have **no adapter**; never operational |
| `GatewayManagementService` | `…/billing/service/` | Configure, test, activate, deactivate, default, methods, webhook status. Provider-neutral |
| `GatewayRoutingService` | `…/billing/service/` | Picks the gateway for a new payment (active, configured, currency, method, default first); finds the payment's own provider afterwards |
| `PaymentService` | `…/billing/service/` | Creates, confirms, reconciles and refunds payments and processes webhooks through the adapter. Contains no provider name |
| `AdminPaymentGatewayController`, `PaymentWebhookController` | `…/billing/controller/` | `/admin/billing/gateways/**` and `POST /webhooks/{provider}` |
| `payment_gateway_config` | database, migration V038 | One row per provider: environment, encrypted credentials, activation, default, methods, test and webhook status ([data model](../07-database/data-model/billing-payments.md#c98-changes-migration-v038)) |
| Frontend | `frontend/src/components/payments/`, `frontend/src/api/paymentGatewayApi.ts` | The list, configure form and details are generated from what the API returns; no provider is named in them |

## Security model
- **Credentials are encrypted at rest.** An administrator can enter, replace and rotate a provider's credentials on the Payment Gateways screen. They are stored as one **AES-256-GCM** ciphertext (a fresh random IV per write) in `payment_gateway_config.credentials_encrypted`.
- **Master key from the environment.** The key is `PAYMENT_GATEWAY_ENCRYPTION_KEY`: the base64 text of **exactly 32 random bytes**. It is never stored in the database or in source control (it belongs in `config/secrets.env` for local work and in the environment's secret store when deployed, like every other secret, [BR-SEC-001](../03-business-rules/BR-SEC-001-central-secrets-file.md)). Generate one with:

  ```bash
  openssl rand -base64 32
  ```

  A key that is missing, not base64 or not 32 bytes is treated as no key.
- **No key, no stored credentials.** Without a usable key the backend refuses to save credential values (HTTP 409) and the Configure dialog says so. Credentials supplied through the secrets file or environment keep working without it.
- **Key loss or change means re-entering credentials.** AES-GCM cannot decrypt with another key, and there is no recovery. After a key is lost or changed, stored credentials read as "unreadable" (the details and the Configure dialog say so), a gateway whose credentials were all stored becomes not configured, and an administrator must enter them again with the new key. Do not rotate the master key casually: there is no re-encryption tool. Keep the key backed up in the secret store.
- **Stored over environment.** For each field a stored value overrides the environment or secrets-file value; clearing is not possible from the screen (a blank keeps the value), so to go back to the secrets file the stored row must be changed by a database administrator.
- **Write-only.** No API response, log line, audit entry or screen shows a credential. The API says only whether each field is set, where it comes from (`STORED` or `ENVIRONMENT`) and, for a non-secret identifier, a masked form. Audit entries name the fields changed, never their values.
- **Permission.** Only `MANAGE_BILLING` can see or change any of this.
- **Webhooks are signature-only.** `POST /webhooks/{provider}` needs no token; the adapter verifies the provider's signature against the raw body. Failures are counted on the gateway row (one counter and the latest reason), not stored one row per attempt.
- **Deployment checklist for a new environment:** (1) apply migration V038; (2) set `PAYMENT_GATEWAY_ENCRYPTION_KEY` **before** entering credentials on screen; (3) set `BACKEND_URL` (the property `app.backend-url`) to the public address of the backend including its `/api` path, for example `https://eis.evyoog.com/api`, because the webhook URL shown on screen is built from it and the screen warns when it points at the local machine; (4) register each provider's webhook ([below](#registering-the-webhook-per-provider)).

## How to add a new provider
The screen, the API, routing and the webhook endpoint need **no change**. What a new provider (call it `ACME`) needs:

1. **Implement `PaymentGateway` as a Spring bean** (`@Component`) in `…/billing/service/gateway/`, like `RazorpayGateway`. Keep every provider-specific thing inside it (HTTP calls, signatures, payload shapes, status words); translate provider statuses into EIS's own (`GatewayPaymentInfo.Status`, `ConnectionStatus`, `GatewayWebhookEvent.Type`).
2. **Write the descriptor** (`descriptor()`):
   - `code`: stable upper-case code, **at most 20 characters** (the `payment.provider` column is `VARCHAR(20)`); it also forms the webhook path (`/webhooks/acme`).
   - `name`, `description`, `logo` key, `countries`, `currencies` (use the currencies EIS supports), `methods` (a subset of `card`, `upi`, `netbanking`, `wallet`, `bank_transfer`, `paypal`; add the label under `admin.paymentGateway.methods.*` in `en.json` and `es.json` if you use a new method), `transactionTypes` (`INVOICE_PAYMENT`), `environments` (`TEST`, `LIVE`), webhook / refund / recurring support, `IMPLEMENTED`.
   - **Credential fields** (`ConfigField.of(...)` for an identifier, `ConfigField.webhookSecret(...)` for the field the webhook check uses): key, label, whether secret, whether required, help text. Mark secrets as secret: a secret field is a password input, never returned and never masked. A provider is **configured** when every required field is set.
   - `setupSteps` (shown numbered on the Webhook tab) and `webhookEvents` (the event names to subscribe to).
3. **Credentials the deployment already supplies** (optional): override `environmentCredentials()` to return them by field key; stored values still win.
4. **Validation and masking** (optional): override `validateConfiguration` (no network call; for example "this key does not match the environment") and `mask` (how a non-secret identifier is shown).
5. **Implement the operations:** `testConnection` (one real, harmless authenticated call; return `AUTHENTICATION_FAILED` for a credential refusal and `PROVIDER_UNAVAILABLE` for an outage, never a simulated success), `createOrder` (return the provider's order id and a **public** `clientConfig` map; never a secret), `verifyPayment`, `fetchPayment`, `refund`, `verifyWebhook` (against the **raw body**, constant-time compare) and `parseWebhook` (a stable event id, event type, order id, payment id, method details).
6. **Delete its entry from `PlannedProviders`** if it had one (the registry already lets an adapter replace the planned entry with the same code, but leaving the old entry is confusing).
7. **Add a logo key.** Add the key to `PaymentBrand` and `BRAND_NAMES` in `frontend/src/components/payments/paymentBrands.ts` and put the official `acme.svg` (and `acme-dark.svg`) in `frontend/src/assets/payment-logos/` with its source and licence in `ATTRIBUTION.md` ([payment-brand-assets.md](../05-ui/screen-requirements/payment-brand-assets.md)). Without a file the screen shows a neutral monogram; that is acceptable until the logo is licensed.
8. **Add the checkout launcher.** `CheckoutPage.tsx` can open only the providers it has a launcher for (today Razorpay Checkout). A payment routed to a provider without a launcher shows an error and charges nothing, so the new provider needs a launcher that uses `provider`, `providerOrderId` and `clientConfig` from the payment response, before it is activated in production.
9. **Tests, with the `FakeGateway` pattern.** `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/FakeGateway.java` is a test-only second provider with no network. Write a similar fake or a unit test per adapter (like `RazorpayGatewayTest`: descriptor, environment rules, status mapping, signature check, payload parsing with a mocked HTTP client) and add the generic cases from `GatewayManagementServiceTest` that matter for the provider. Add test cases under `test-cases/functional/payment-gateways/` ([TESTPLAN-BIL-003](../../test-cases/functional/payment-gateways/TESTPLAN-BIL-003.md)), and update this page and [business-rules.md](../02-requirements/FRD/payment-gateways/business-rules.md) if the provider needs a rule of its own (as Razorpay's BR-14).
10. **Approve the provider first.** Rule 2 of `CLAUDE.md`: do not build a provider that is not approved. Record it in `docs/01-business/roadmap/open-decisions.md` (which provider follows Razorpay is Not specified).

A **real** integration needs a real test: run **Test connection** against the provider's sandbox and a sandbox payment and webhook before activating in production (see [Not done](#what-is-not-done)).

## Registering the webhook per provider
Each provider has its own endpoint: `POST <backend URL>/webhooks/<provider code in lower case>`. The exact URL, the events to subscribe to, the signature secret field and the set-up steps are shown on **Payment Gateways → View details → Webhook** for that provider, and can be copied from there. The backend URL is the property `app.backend-url`, environment variable `BACKEND_URL` (in production `https://eis.evyoog.com/api`).

| Provider | URL | Signature | Events |
|---|---|---|---|
| Razorpay | `<backend URL>/webhooks/razorpay` (unchanged since [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md)) | Header `X-Razorpay-Signature`, HMAC with the webhook secret you choose in the Razorpay Dashboard and enter in the **Webhook secret** field | `payment.captured`, `payment.failed`, `refund.processed` |
| Any new provider | `<backend URL>/webhooks/<code>` | As implemented in its adapter's `verifyWebhook` | Its descriptor's `webhookEvents` |

Notes:
- Register the webhook **for every environment** (Test and Live have separate dashboards and separate secrets).
- The backend must be reachable from the provider over the internet; a `localhost` address is flagged on screen.
- A webhook is accepted for a configured provider even after it is deactivated, so payments already in flight finish. Keep the webhook registered until they are done.
- A delivery that fails verification is answered 400 and counted on the gateway ("n rejected webhook(s)" and the last reason on the Webhook tab). Seeing "Signature verified" there means a correctly signed event has really arrived.

## What is NOT done
- **No provider other than Razorpay.** Stripe, PayPal, PayU and Cashfree are placeholders: described, never configurable, testable or activatable. Which provider follows Razorpay, and when, is Not specified.
- **No routing by country, amount or cost.** Routing uses currency, payment method, activation and the default only. The countries and transaction types in the registry are descriptive.
- **No automatic fail-over.** If the chosen provider errors, the payment fails; it is not retried on another gateway.
- **No recurring charges through a provider.** Renewals are not charged automatically; each provider's token or mandate flow is Not specified.
- **Saved-card tokens (Payment methods) work for Razorpay only.**
- **No re-encryption of stored credentials** when the master key changes (credentials are entered again).
- **Real-provider checks are manual.** The automated tests use a mocked Razorpay client and a fake provider. A real Razorpay connection test and a real signed webhook delivery have not been run by the test suite ([test cases](../../test-cases/functional/payment-gateways/TESTPLAN-BIL-003.md) mark them Not Run).
- **The checkout can open only Razorpay's payment window** today (see step 8 above).
