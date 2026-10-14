# REQ-BIL-003 — Payment gateway management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner, 2026-10-14 ([C98](../../../01-business/roadmap/open-decisions.md#c98))
**Decision:** [C98](../../../01-business/roadmap/open-decisions.md#c98), extends [C97](../../../01-business/roadmap/open-decisions.md#c97) BI-8

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) (carry-over work) |
| Requirement ID | REQ-BIL-003 |
| Application | [08 Billing & Payments](../../../01-business/roadmap/applications/08-billing-payments.md) |
| Application code | `APP-BIL` |
| Priority | P0 |
| AI required | No |

## Summary
The Platform Admin page `/admin/billing/payment-gateway` ("Payment Gateways") manages every payment provider EIS can collect payments through. It no longer assumes Razorpay. Providers are adapters behind one backend interface; the page is driven by what the provider registry and the database say. Credentials, environment, payment methods, webhook status and validation are per gateway. Several gateways can be active at once and one can be the default.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-003.1 | The page shows an overview (registered, configured, active, needing configuration) and the list of gateways, all from backend data. Each row shows provider, supported methods and currencies, environment, configuration status, connection status, activation status, default marker, last test result and only the actions valid for its state. | Must |
| REQ-BIL-003.2 | A provider registry holds, per provider: id, name, description, logo key, countries, currencies, methods, transaction types, test and live support, webhook, refund and recurring support, configuration fields, setup steps and implementation status (IMPLEMENTED or PLANNED). Adding an adapter adds a row to the page with no UI change. | Must |
| REQ-BIL-003.3 | A PLANNED provider is listed as not yet available: description and required fields can be viewed; it cannot be configured, tested or activated, and is never shown as operational. | Must |
| REQ-BIL-003.4 | Configure shows a form built from the provider's field list (secret fields are write-only, a blank value keeps the stored one). The API never returns a secret; it returns, per field, whether it is set, its source (stored or environment) and a masked public identifier. | Must |
| REQ-BIL-003.5 | Credentials are encrypted at rest with a master key from the environment. If the key is missing, saving is refused with a clear message. Credentials are never logged, audited or committed. | Must |
| REQ-BIL-003.6 | Test connection calls the real provider with the stored credentials and shows the actual outcome: not configured, configuration incomplete, connection successful, authentication failed, provider unavailable, unsupported configuration, or test failed. The result is stored with the configuration version it tested. | Must |
| REQ-BIL-003.7 | Configured, Connected, Active and Default are separate statuses. A gateway can be activated only when its configuration is complete and a live test of the current configuration passes. Deactivate is always allowed. A default must be active; deactivating the default clears it. | Must |
| REQ-BIL-003.8 | Changing the credentials of an active gateway re-tests it in the same request; if the test fails the change is not saved. | Must |
| REQ-BIL-003.9 | Evidence flags are shown separately from the connection test: authenticated, webhook signature seen, payment captured, refund made. | Should |
| REQ-BIL-003.10 | Webhooks: `POST /webhooks/{provider}`; the provider's adapter verifies the signature on the raw body; an invalid signature is rejected and counted; events are idempotent per provider and event id; accepted for any configured provider even if deactivated. The page shows the endpoint (from `app.backend-url`), the provider's setup steps, the last event and recent failures. | Must |
| REQ-BIL-003.11 | Payment methods: per gateway, the administrator can switch off methods the provider supports; unsupported methods are never offered. Checkout shows only methods that an eligible gateway offers and Billing settings allows. | Must |
| REQ-BIL-003.12 | Routing: for each new payment the backend selects an eligible gateway (active, configured, supports the invoice currency and the chosen method; default first). With none, the payment is refused with a clear error. The frontend never selects a gateway. | Must |
| REQ-BIL-003.13 | A payment records its provider. Confirm, reconcile and refund use the payment's own provider. Changing activation or the default never changes existing payments, refunds or subscriptions. | Must |
| REQ-BIL-003.14 | Existing Razorpay behaviour, history, routes and permissions keep working. Razorpay credentials in the secrets file are still used unless a stored value exists. After migration Razorpay stays active and default. | Must |
| REQ-BIL-003.15 | Every administrator change (configure, rotate, test, activate, deactivate, default, methods) is audited with the actor and the gateway, never the values. | Must |

## Out of scope
Real adapters for Stripe, PayPal, PayU and Cashfree; routing by country, amount or cost; automatic fail-over; recurring charges through a provider; saved-card tokens for any provider other than Razorpay.

## Where each part lives
Screen: [admin-payment-gateway.md](../../../05-ui/screen-requirements/admin-payment-gateway.md). API: [payment-gateways.md](../../../06-api/api-requirements/payment-gateways.md). Data: [billing-payments.md](../../../07-database/data-model/billing-payments.md). Test cases: `test-cases/functional/payment-gateways/`.
