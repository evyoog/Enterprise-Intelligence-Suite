# Data model — Tax Rules and Calculation

Requirement: [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md). Migration `database/migrations/V036__tax_engine.sql`, mirrored in `backend/src/main/resources/db/schema.sql`. Amounts are in the currency's smallest unit. **No tax rate is stored as seed data.**

## tax_settings (one row, id = 1)
`external_engine_enabled` (default false), `config_version` (+1 on every mode or provider-settings change), `allow_self_approval`, `provider_code`, `provider_company_code`, `provider_environment`, `provider_timeout_seconds` (2–60), `provider_max_attempts` (1–5), `validation_status` (`NOT_VALIDATED`/`PASSED`/`FAILED`), `validated_at`, `validation_summary`, `provider_countries`, `provider_currencies`, `provider_transaction_types` (comma separated), `mode_changed_at`, `mode_changed_by`, `updated_at`, `updated_by`. **No credential column.**

## tax_class, tax_registration
`tax_class`: `code` (unique), `name`, `hsn_sac_code`, `description`, `enabled`. `products.tax_class_code` points at a class code.
`tax_registration`: `country_code`, `state_code`, `tax_type`, `registration_number`, `effective_from`, `effective_to`, `enabled`.

## tax_rule and tax_rule_component
`tax_rule`: `rule_key` (the family), `version`, `name`, `country_code`, `state_code` (normalised key, null = whole country), `jurisdiction`, `tax_type` (`GST`, `VAT`, `SALES_TAX`, `OTHER`), `product_class_code`, `customer_type` (`ANY`, `INDIVIDUAL`, `BUSINESS`), `supply_type` (`ANY`, `INTRA_STATE`, `INTER_STATE`, `CROSS_BORDER`), `tax_id_condition` (`ANY`, `PROVIDED`, `NOT_PROVIDED`), `treatment` (`TAXABLE`, `EXEMPT`, `ZERO_RATED`, `NIL_RATED`, `REVERSE_CHARGE`, `OUT_OF_SCOPE`), `effective_from`, `effective_to`, `approval_status` (`DRAFT`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`), `active`, `notes`, `rejection_reason`, `created_by`, `last_edited_by`, `submitted_at`, `approved_by`, `approved_at`. Unique `(rule_key, version)`; `effective_to >= effective_from`.
`tax_rule_component`: `rule_id`, `sort_order`, `code`, `name`, `rate` (`NUMERIC(9,4)`, 0–100), `compound`; unique `(rule_id, code)`.

## tax_exemption
Owner (exactly one of `owner_customer_id`, `owner_organization_id`), `country_code`, `reason`, `certificate_number`, `effective_from`, `effective_to`, `status` (`PENDING_APPROVAL`, `APPROVED`, `REVOKED`), `created_by`, `approved_by`, `approved_at`.

## tax_calculation
One row per attempt: `invoice_id`, `transaction_ref`, `transaction_type`, `engine`, `config_version`, `status` (`CALCULATED`/`BLOCKED`), `reason_code`, `reason_message`, `provider_code`, `provider_reference`, `attempts`, `currency`, `taxable_total`, `tax_total`, `response_copy` (the validated provider answer; never a credential), `calculated_at`.

## invoice, invoice_line (additions)
`invoice.status` gains `DRAFT`. New: `tax_engine`, `tax_config_version`, `tax_status` (`LEGACY` default for rows before C96, `CALCULATED`, `BLOCKED`), `tax_block_code`, `tax_block_reason`, `tax_calculation_id`, `place_of_supply`, `issuer_snapshot`, `customer_tax_id`. `issued_at` is set again when a Draft is finalized.
`invoice_line`: `tax_class_code`, `hsn_sac_code`, `tax_amount`.

## invoice_tax_line
`invoice_id`, `invoice_line_id`, `sort_order`, `component_code`, `component_name`, `tax_type`, `jurisdiction`, `treatment`, `rate`, `taxable_amount`, `tax_amount`, `rule_id`, `rule_version`, `note`. Copied at calculation time, never edited.

## credit_note, credit_note_tax_line
`credit_note`: `credit_note_number` (unique), `invoice_id`, owner ids, `currency`, `subtotal`, `tax_amount`, `total`, `reason`, `tax_engine`, `payment_refund_id`, `issued_at`, `issued_by`.
`credit_note_tax_line`: `credit_note_id`, `sort_order` (the original line's order), component fields, `rate`, `taxable_amount`, `tax_amount`.

## document_number_sequence
`(series, financial_year)` primary key, `next_value`. Series `INV` (invoices) and `CN` (credit notes). Financial year `26-27` = 1 April 2026 to 31 March 2027, India time. Numbers: `EIS/26-27/000123`, `EIS-CN/26-27/000001`.

## billing_settings (defaults)
New default `invoice_prefix = 'EIS'` and `payment_terms_days = 15` (BI-2). The migration moves a row still on the old defaults (`INV`, 0); numbers already issued never change.
