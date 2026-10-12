# Data model — Projects (REQ-TEN-009)

Migration `database/migrations/V028__projects.sql`, mirrored in `schema.sql`.

## organization_group (extended)
New columns: `kind` (`GROUP` default, or `PROJECT`), `description` (up to 500). A project is a group of kind `PROJECT`; members stay in `organization_group_member`.

## organization_group_product
id, group_id → organization_group (cascade), product_id → products, product_role (up to 50), added_at; unique (group_id, product_id).

## organization_product_access (extended)
New column `source_group_id` → organization_group. Empty means a direct assignment; set means the access comes from that project (BR-PRJ-004 to 006).
