# Data model — Access management

[REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md). Migration (when approved and built): `database/migrations/V020__access_management.sql`, mirrored in `backend/src/main/resources/db/schema.sql`.

## organization_role_default
Role defaults per organization. No row for an item = the platform role definition applies (Open question 9).

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| organization_id | Yes | FK organization |
| org_role | Yes | `ORG_ADMIN` or `MEMBER` |
| item_type | Yes | `PERMISSION` or `PRODUCT` |
| permission_code | When `PERMISSION` | Feature permission code |
| product_id | When `PRODUCT` | FK products |
| enabled | Yes | Default ON or OFF |
| updated_by_customer_id, updated_at | Yes | Who and when |

Unique (organization_id, org_role, item_type, permission_code, product_id).

## member_access_override
Individual overrides.

| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| organization_member_id | Yes | FK organization_member |
| item_type | Yes | `PERMISSION` or `PRODUCT` |
| permission_code | When `PERMISSION` | Feature permission code |
| product_id | When `PRODUCT` | FK products |
| granted | Yes | `true` = ON, `false` = OFF |
| set_by_customer_id | Yes | Who set it |
| set_at | Yes | When |

Unique (organization_member_id, item_type, permission_code, product_id).

## Product access rows
`organization_product_access` (existing) stays the record of a member's access to a product and its product role; an effective-access change for a product creates or deactivates that row (the product role is Open question 7).

## New permission codes
Seeded on the `ORG_ADMIN` role and added to the protected permission list: `MANAGE_SUBSCRIPTIONS`, `MANAGE_ACCESS`, and — if confirmed — the organization billing permission (Open question 2). `MANAGE_ORDERS` is also added to the protected list (inventory finding).

## Audit
No new table: changes are written to `audit_log` with actions `ACCESS_OVERRIDE_SET`, `ACCESS_OVERRIDE_RESET`, `ACCESS_ROLE_DEFAULT_CHANGED`, `ACCESS_RECALCULATED`, `ACCESS_BULK_CHANGED`.
