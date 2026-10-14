# Data model — Billing & Payments

> **Sprint 2026.4.3 additions ([C97](../../01-business/roadmap/open-decisions.md#c97)):** migration [`V037__billing_rules_sprint_2026_4_3.sql`](../../../database/migrations/V037__billing_rules_sprint_2026_4_3.sql), mirrored in `backend/src/main/resources/db/schema.sql` — see [C97 changes](#c97-changes-migration-v037). Flyway is disabled: apply V034 to V037 by hand, in order.

> **Payment gateways ([C98](../../01-business/roadmap/open-decisions.md#c98), REQ-BIL-003):** migration [`V038__payment_gateways.sql`](../../../database/migrations/V038__payment_gateways.sql), mirrored in `backend/src/main/resources/db/schema.sql` — new table `payment_gateway_config` and a per-provider `payment_webhook_event` — see [C98 changes](#c98-changes-migration-v038). Apply V038 by hand after V037.

> **Administrator-added payment gateways ([C99](../../01-business/roadmap/open-decisions.md#c99), REQ-BIL-003.16 and .17):** migration [`V039__payment_gateway_custom_definitions.sql`](../../../database/migrations/V039__payment_gateway_custom_definitions.sql), mirrored in `backend/src/main/resources/db/schema.sql` — three columns on `payment_gateway_config` — see [C99 changes](#c99-changes-migration-v039). Apply V039 by hand after V038.

Proposed for [REQ-BIL-001](../../02-requirements/FRD/billing-payments/requirement.md) and [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md). Final column types are set in the migration when the feature is built. Workbook entities used: Invoice (DE), Payment (DE). No card numbers or CVV are stored anywhere ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).

Ownership: every record belongs to **either** a customer (individual) **or** an organization, never both.

## billing_details
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| customer_id / organization_id | One of them | Owner |
| billing_name, billing_email | Yes | |
| address_line1, address_line2, city, state, postal_code, country | Yes (line 2 optional) | |
| tax_id | No | For example GSTIN; validation Not specified |
| updated_at, updated_by | Yes | |

## invoice
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| invoice_number | Yes (null while a Draft) | Unique, immutable once finalized; format `EIS/26-27/000123` ([C97](../../01-business/roadmap/open-decisions.md#c97) BI-2) |
| customer_id / organization_id | One of them | Owner |
| subscription_id | Yes | What it bills |
| status | Yes | DRAFT (tax could not be determined, C96), OPEN, PAID, PARTIALLY_REFUNDED, REFUNDED, VOID |
| currency | Yes | Plan currency |
| subtotal, tax_amount, total | Yes | Smallest currency unit |
| tax_method_used | Yes | `ADMIN_RATE`, `TAX_SERVICE`, or null when no tax rule applied ([REQ-BIL-002.3](../../02-requirements/FRD/tax-rules/requirement.md)) |
| payment_route | No | `ONLINE`, `OFFLINE` — what the customer chose at checkout ([C55](../../01-business/roadmap/open-decisions.md#c55), REQ-BIL-001.19); null until a route is chosen |
| period_start, period_end, issued_at, due_at | Yes | due_at = issued_at + payment terms (default 15 days, BI-2) |
| purchase_order_number | No | Customer's optional purchase-order number, up to 50 characters, printed on the invoice (BI-9). V037 |
| bill_to_snapshot | Yes | Billing details copied at issue |

## invoice_line
id, invoice_id, description, period_start, period_end, quantity, unit_amount, amount, **tax_name**, **tax_rate**, **tax_amount** (the last three copied from the tax rule in force at issue time, per [REQ-BIL-002.5](../../02-requirements/FRD/tax-rules/requirement.md); null/0 when no rule applied).

## tax_rule
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| region_id | Yes | FK to `platform_region` ([REQ-GOV-001.2](../../02-requirements/FRD/platform-administration/requirement.md)) |
| tax_name | Yes | For example "GST" |
| rate | Yes | Percentage, 0–100, up to 2 decimals |
| method | Yes | `ADMIN_RATE`, `TAX_SERVICE` |
| effective_from | Yes | Date this rate takes effect |
| status | Yes | ENABLED, DISABLED |
| updated_at, updated_by | Yes | |

At most one ENABLED row per (region_id, effective_from) — [BR-2](../../02-requirements/FRD/tax-rules/business-rules.md).

## payment
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| invoice_id | Yes | |
| provider | Yes | The code of the provider that created the payment (`RAZORPAY` today; any implemented provider's code, [C98](../../01-business/roadmap/open-decisions.md#c98)) or `OFFLINE`. Confirm, reconcile and refund go to this provider. Column is `VARCHAR(20)`, so a provider code must be at most 20 characters |
| provider_order_id, provider_payment_id | Yes / when known | The provider's IDs (Razorpay IDs today) |
| status | Yes | CREATED, CAPTURED, FAILED, PARTIALLY_REFUNDED, REFUNDED |
| amount, refunded_amount, currency | Yes | Smallest unit |
| method_type, method_network, method_last4 | When known | Display only. `method_type` gains `OFFLINE` (C55) |
| failure_reason | No | As returned by Razorpay |
| created_at, captured_at | | |
| offline_method | OFFLINE only | `BANK_TRANSFER`, `NEFT_RTGS`, `CHEQUE` (REQ-BIL-001.20) |
| offline_reference | OFFLINE only | Bank/cheque reference number |
| received_on | OFFLINE only | Date the money was received (not in the future) |
| recorded_by | OFFLINE only | Admin who recorded it |
| note | No | Admin note |

For an OFFLINE payment, `provider` is `OFFLINE` and the Razorpay IDs are null.

## billing_settings
Single row, platform-wide (REQ-BIL-001.21; extended by [C60](../../01-business/roadmap/open-decisions.md#c60), migration `V016__billing_settings_full.sql`). Not secrets — never in the secrets file (Razorpay keys stay in `config/secrets.env`, BR-SEC-001).

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| business_legal_name, business_trade_name, business_gstin, business_pan, business_cin | Legal name when set | Invoice issuer (C60) |
| business_address_line1/2, business_city, business_state, business_postal_code, business_country | Line 1, city, state, postal code, country when set | Registered address |
| business_email, business_phone, business_website | Email when set | Billing contact |
| invoice_prefix | Yes (default `INV`) | Prefix of new invoice numbers |
| payment_terms_days | Yes (default 0, 0–365) | Due date = issue date + days |
| invoice_footer_note | No | Printed on invoices and receipts |
| offline_account_name, offline_bank_name, offline_account_number | When set | Printed on offline invoices |
| offline_branch_name, offline_account_type (CURRENT/SAVINGS), offline_ifsc, offline_micr, offline_swift_bic, offline_iban, offline_upi_id | No | More bank details (C60) |
| offline_cheque_payable_to, offline_cheque_address, offline_instructions | No | Cheque and instructions (C60) |
| offline_bank_transfer_enabled, offline_neft_rtgs_enabled, offline_cheque_enabled | Yes (default true) | Accepted offline methods |
| method_card_enabled, method_upi_enabled, method_netbanking_enabled, method_wallet_enabled, method_pay_by_invoice_enabled | Yes (default true) | Checkout methods |
| checkout_display_name, checkout_description, checkout_theme_color | No | Razorpay Checkout appearance |
| terms_url, privacy_url, refund_policy_url | No | Links to the Terms, Privacy and Refund pages on evyoog.com, up to 500 characters each (BI-7). V037 |
| active_gateway | Yes (default `RAZORPAY`) | Code of the payment gateway checkout used (BI-8). V037. **No longer used by routing since [C98](../../01-business/roadmap/open-decisions.md#c98)**: activation and default now live in `payment_gateway_config`. The column is left in place and is not read or written; do not rely on it |
| updated_at, updated_by_customer_id | Yes | |

## payment_refund
id, payment_id, provider_refund_id, amount, reason, status, requested_by, created_at.

## payment_method
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| customer_id / organization_id | One of them | Owner |
| provider_token_ref | Yes | Razorpay token / method reference only |
| type | Yes | CARD, UPI |
| network, last4, expiry_month, expiry_year, card_type, issuer | Cards | Display only |
| upi_masked | UPI | Display only |
| is_default | Yes | One default per owner |
| consent_at | Cards | When the customer agreed to save |
| status | Yes | ACTIVE, REMOVED |

## payment_webhook_event
id, **provider** (`VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY'`, V038), provider_event_id, event_type, payment_id (nullable), received_at, processed_at, payload_summary (no card data). **Unique on (`provider`, `provider_event_id`)** (`uq_payment_webhook_event_provider`, V038): an event is processed once per provider, so the same event id from two providers is two events. The earlier single-column unique constraint on `provider_event_id` is dropped. Existing rows get `RAZORPAY`.

## C97 changes (migration V037)
All additive, `IF NOT EXISTS`, safe to re-run. Tables and columns:

| Table | Change | Purpose | Requirement |
|---|---|---|---|
| organization | `pay_by_invoice_enabled BOOLEAN NOT NULL DEFAULT false` | The platform administrator enables Pay by invoice organization by organization (BR-18). The migration then **sets it true for every organization that already has an invoice with `payment_route = 'OFFLINE'`**, so their open invoices stay payable | REQ-BIL-001.28, BI-4 |
| billing_settings | `terms_url`, `privacy_url`, `refund_policy_url` VARCHAR(500), nullable | Policy page links (BR-20) | REQ-BIL-001.30, BI-7 |
| billing_settings | `active_gateway VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY'` | Active payment gateway (BR-21) | REQ-BIL-001.31, BI-8 |
| invoice | `purchase_order_number VARCHAR(50)`, nullable | Optional purchase-order number (BR-22) | REQ-BIL-001.32, BI-9 |
| invoice_reminder | **New table** | Records that a payment reminder was sent, so none is sent twice (BR-17) | REQ-BIL-001.27, BI-2 |
| product_subscription | `pending_plan_id BIGINT`, `pending_plan_requested_at TIMESTAMP`, nullable | Scheduled downgrade ([subscription-seats.md](subscription-seats.md#scheduled-plan-change-columns)) | REQ-SUB-001.8, SC-1 |

### invoice_reminder
| Attribute | Required | Description |
|---|---|---|
| id | Yes | BIGSERIAL primary key |
| invoice_id | Yes | FK to `invoice(id)`, `ON DELETE CASCADE` |
| kind | Yes | `BEFORE_DUE`, `ON_DUE` or `AFTER_DUE` (CHECK) — 3 days before, the due date, 7 days after |
| sent_at | Yes | Default `now()` |

Unique on (`invoice_id`, `kind`) (`uq_invoice_reminder`): one row per invoice and kind.

## C98 changes (migration V038)
Additive and safe to re-run (`IF NOT EXISTS`, `DROP CONSTRAINT IF EXISTS`, `ON CONFLICT DO NOTHING`). Mirrored in `backend/src/main/resources/db/schema.sql`. Requirement: [REQ-BIL-003](../../02-requirements/FRD/payment-gateways/requirement.md); rules: [business-rules.md](../../02-requirements/FRD/payment-gateways/business-rules.md).

| Table | Change | Purpose |
|---|---|---|
| payment_gateway_config | **New table**, below | One row per payment provider: environment, encrypted credentials, activation, default, enabled methods, last test, webhook status |
| payment_webhook_event | `provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY'` added; unique `uq_payment_webhook_event_provider (provider, provider_event_id)` replaces the unique on `provider_event_id` alone | Idempotency per provider (BR-13) |
| payment_gateway_config | `INSERT … ('RAZORPAY', true, true)` | An existing installation keeps taking payments: Razorpay is active and default after the migration (BR-17) |

### payment_gateway_config
| Attribute | Required | Description |
|---|---|---|
| id | Yes | BIGSERIAL primary key |
| provider_code | Yes | Unique, up to 30 characters; the registry's provider code in upper case (`RAZORPAY`) |
| environment | No | `TEST` or `LIVE` (CHECK); null until chosen (a configuration that predates the screen) |
| activated | Yes | Default false. Accepting new payments (BR-8) |
| default_gateway | Yes | Default false. Tried first by routing; at most one active gateway holds it (BR-8) |
| credentials_encrypted | No | **AES-256-GCM ciphertext** (base64 of a 12-byte IV followed by the ciphertext and tag) of one JSON object of field key to value. Never returned by any API and never logged. Null when nothing is stored. The master key is the environment variable `PAYMENT_GATEWAY_ENCRYPTION_KEY`, **not stored anywhere in the database** (BR-3) |
| credentials_updated_at | No | When credentials were last stored |
| config_version | Yes | Default 1; incremented by every save of credentials or environment. A test result counts only for the version it tested (BR-6) |
| enabled_methods | No | Comma-separated methods switched on; null means every method the provider supports (BR-15) |
| test_status, test_message, tested_at, tested_config_version | No | Outcome of the last connection test (a `ConnectionStatus` name, a message of up to 300 characters, when, and which `config_version`) |
| last_webhook_at, last_webhook_event_type | No | The last accepted, correctly signed webhook |
| last_webhook_failure_at, last_webhook_failure_reason, webhook_failure_count | Yes for the count (default 0) | **One counter and the latest reason, not a row per attempt** (BR-13). The count is never reset |
| created_at, updated_at, updated_by_customer_id | Yes / Yes / No | Housekeeping. `updated_by_customer_id` is null when the administrator's Keycloak account has no customer row |

A row is created on first use for **every** built-in provider the registry lists (inactive, nothing stored); a gateway an administrator adds gets its row when it is added (C99, below); and the Razorpay row is created active and default when it is missing. Credentials the deployment supplies through the secrets file are **not** copied here; a stored value overrides them field by field (BR-5).

## C99 changes (migration V039)
Additive and safe to re-run (`ADD COLUMN IF NOT EXISTS`); no data is changed. Mirrored in `backend/src/main/resources/db/schema.sql`. Requirement: [REQ-BIL-003.16, .17](../../02-requirements/FRD/payment-gateways/requirement.md); rules [BR-20 to BR-25](../../02-requirements/FRD/payment-gateways/business-rules.md). Apply after V038.

| Table | Column | Type | Description |
|---|---|---|---|
| payment_gateway_config | `custom` | `BOOLEAN NOT NULL DEFAULT false` | True for a gateway an administrator added from the Payment Gateways screen (no adapter, configuration only, BR-23). False for the Razorpay row and for every row that existed before V039 |
| payment_gateway_config | `display_name` | `VARCHAR(100)` | The name the administrator gave an added gateway. Null for a built-in provider (its name comes from its adapter) |
| payment_gateway_config | `definition` | `TEXT` | The **non-secret** definition of an added gateway as JSON (`CustomGatewayDefinition`): description, logo key, countries, currencies, methods, environments and the credential field list (key, label, secret, required, never a value). Null for a built-in provider. **Never holds a credential value** |

- An added gateway's **credential values** stay in `credentials_encrypted` (AES-256-GCM, [BR-3](../../02-requirements/FRD/payment-gateways/business-rules.md)), keyed by the field keys of the definition; removing a field in the definition deletes its value from the ciphertext (BR-21). Removing the gateway deletes the whole row (BR-24).
- `provider_code` (unique, up to 30 characters) is the gateway's code, 2 to 30 characters from the Add form. Note: `payment.provider` is `VARCHAR(20)` and `payment_webhook_event.provider` `VARCHAR(30)`, so a gateway whose code is 21 to 30 characters can be stored here but cannot be given an adapter without a shorter code ([09-integrations/payment-gateways.md](../../09-integrations/payment-gateways.md#how-to-turn-an-added-gateway-into-a-real-integration)).
- An added gateway never has `activated` or `default_gateway` true, never a `test_status`, and never a payment or webhook event row (it has no adapter, BR-23). If an adapter is later written for its code, the same row (credentials, environment, `config_version`) is reused by that adapter and `custom` stays true but is ignored.
- The unique index on `provider_code` also stops two concurrent adds of the same code (the second is answered 409).

Not stored: the payment gateway's credentials supplied through the environment or secrets file (BR-SEC-001), and no licence keys or usage figures anywhere ([REQ-SUB-002](../../02-requirements/FRD/entitlements/requirement.md) is derived, with no table). Subscription activation after payment (BI-1) adds no column: it uses the existing `product_subscription.status` (`PENDING_SUBSCRIPTION` to `ACTIVE`) and `started_at`.
