# Data model — Billing & Payments

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
| invoice_number | Yes | Unique, immutable once finalized; format Not specified |
| customer_id / organization_id | One of them | Owner |
| subscription_id | Yes | What it bills |
| status | Yes | OPEN, PAID, PARTIALLY_REFUNDED, REFUNDED, VOID |
| currency | Yes | Plan currency |
| subtotal, tax_amount, total | Yes | Smallest currency unit |
| tax_method_used | Yes | `ADMIN_RATE`, `TAX_SERVICE`, or null when no tax rule applied ([REQ-BIL-002.3](../../02-requirements/FRD/tax-rules/requirement.md)) |
| payment_route | No | `ONLINE`, `OFFLINE` — what the customer chose at checkout ([C55](../../01-business/roadmap/open-decisions.md#c55), REQ-BIL-001.19); null until a route is chosen |
| period_start, period_end, issued_at, due_at | Yes (due_at rule Not specified) | |
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
| provider | Yes | `RAZORPAY` |
| provider_order_id, provider_payment_id | Yes / when known | Razorpay IDs |
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
Single row, platform-wide (REQ-BIL-001.21). Not secrets — never in the secrets file.

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| offline_account_name, offline_bank_name, offline_account_number | When set | Printed on offline invoices |
| offline_ifsc, offline_swift_bic | No | Field list to confirm (FRD Open question 11) |
| updated_at, updated_by | Yes | |

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
id, provider_event_id (unique), event_type, payment_id (nullable), received_at, processed_at, payload_summary (no card data).
