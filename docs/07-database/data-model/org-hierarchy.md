# Data model — Organization hierarchy (REQ-TEN-006)

Migration `database/migrations/V025__org_hierarchy.sql` (mirrored in `backend/src/main/resources/db/schema.sql`). Additive.

| Table | Columns | Notes |
|---|---|---|
| `org_node` | id, organization_id → organization, parent_id → org_node (RESTRICT), name (150), node_type (50), code (50), description (1000), sort_order, active, created_at, updated_at | Unique root per organization (partial index); unique sibling name, case-insensitive (partial index). Index on (organization_id, parent_id). |
| `org_level` | id, organization_id, node_type (50), label (100), level_rank | Unique (organization_id, node_type) and (organization_id, level_rank). Default rows are created with the root. |
| `org_node_history` | id, organization_id, org_node_id → org_node (CASCADE), previous_parent_id, new_parent_id, changed_by_customer_id, effective_at | Append-only. |
| `organization_member.org_node_id` | nullable → org_node, ON DELETE SET NULL | The member's home node. |
