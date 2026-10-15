# Database design — `eis_platform`

- **Engine:** PostgreSQL 16
- **Database / schema:** `vyoog` / `eis_platform`
- **Current source of truth:** `backend/src/main/resources/db/schema.sql` (31 tables). Flyway is disabled, so the schema is applied by hand.
- **Going forward:** schema changes are also added as versioned scripts in `database/migrations/` (see its README).

## Table groups
| Group | Tables |
|-------|--------|
| Catalog | `products`, `product_plans`, `platforms`, `product_platforms` |
| Identity & sessions | `customer`, `sso_bridge_session`, `password_reset_token`, `customer_mfa`, `mfa_recovery_code`, `mfa_login_challenge` |
| Tenancy & subscriptions | `organization`, `organization_member`, `organization_product_access`, `product_subscription`, `email_verification_token` |
| SAML federation | `saml_identity_provider`, `saml_external_identity`, `saml_login_request` |
| Authorization | `role`, `permission`, `role_permission`, `privileged_access_request`, `privileged_access_audit_entry` |
| Experience | `favorite_product`, `product_usage`, `dashboard_preference`, `customer_preference`, `search_history_entry` |
| Notifications | `notification`, `notification_preference` |
| Service status, support, SLA, incidents and problems (V003, V010, V041) | `product_service_status`, `support_ticket` (with the SLA deadlines), `service_incident`, `service_incident_ticket`, `service_instance`, `service_reporter_key`, `sla_policy`, `sla_settings`, `problem`, `problem_action` — see [service-instances-sla-incidents.md](data-model/service-instances-sla-incidents.md) |
| Free trials (V042) | No new table: columns `products.trial_days`, `product_subscription.trial` and `trial_reminded_at`; a demo request is a `support_ticket` with the category "Demo request" — see [trials.md](data-model/trials.md) |
| Audit | `audit_log` |

## Conventions
- `BIGSERIAL` surrogate primary keys; `created_at` / `updated_at` timestamps in UTC.
- Enums are stored as `VARCHAR` and mapped with `@Enumerated(EnumType.STRING)` in the backend.
- Every organization-owned row carries its organization id, and access is scoped to it (tenant isolation).

ER diagrams: `ERD/`. Entity definitions: `data-model/`.
