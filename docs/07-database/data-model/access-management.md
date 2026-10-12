# Data model — Access management

[REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md). Migration: `database/migrations/V030__access_management.sql` (built 2026-10-12, [C90](../../01-business/roadmap/open-decisions.md#c90)); the table `member_access_override` was created by V026 (invitations) with permission items only, V030 adds product items. Mirrored in `backend/src/main/resources/db/schema.sql`.

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

Two partial unique indexes (permission rows and product rows). A value equal to the platform definition (permissions) or ON (products) is stored as no row.

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

V030 adds `product_id` (FK products, on delete cascade), makes `permission_code` nullable and replaces the old unique constraints by two partial unique indexes: one per (member, permission) for `PERMISSION` rows and one per (member, product) for `PRODUCT` rows. Existing direct product access (rows without a project source) is copied as `PRODUCT` overrides (granted = row ACTIVE) so nobody loses or gains access by the default-ON rule except members who never had a row.

## Product access rows
`organization_product_access` (existing) stays the record of a member's access to a product and its product role; an effective-access change for a product creates or deactivates that row (the product role is Open question 7).

## New permission codes
Seeded on the `ORG_ADMIN` role and added to the protected permission list: `MANAGE_SUBSCRIPTIONS`, `MANAGE_ACCESS` and `MANAGE_ORGANIZATION_BILLING`. `MANAGE_ORDERS` is also added to the protected list (inventory finding).

## Audit
No new table: changes are written to `audit_log` with actions `ACCESS_PERMISSION_CHANGED`, `ACCESS_PRODUCT_CHANGED`, `ACCESS_OVERRIDE_RESET`, `ACCESS_ROLE_DEFAULT_CHANGED`, `ACCESS_BULK_CHANGED`; recalculated product rows write the existing `PRODUCT_ACCESS_GRANTED` / `PRODUCT_ACCESS_REVOKED`.

## Seats
Named seats per product are counted, not stored: ACTIVE `organization_product_access` rows of ACTIVE members for the product, against the quantity of the organization's ACTIVE subscription to it.
