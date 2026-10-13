# API requirements — Platform ↔ tool synchronization

The full wire contract is [platform-tool-contract-v1.md](../../../09-integrations/platform-tool-contract-v1.md). Summary:

**Platform → tool (MCP tools served by each tool at `/api/mcp`):** `provision_tenant`, `upsert_organization`, `upsert_org_node`, `delete_org_node`, `upsert_user`, `set_membership`, `set_subscription`, `set_user_access`, `get_state_digest`, `list_aggregate_versions`.

**Tool → platform (MCP tools served by the platform at `/api/mcp`):** `report_user_created`, `update_user_profile`, `create_org_node`, `update_org_node`, `move_org_node`, `delete_org_node`, `get_entitlement`, `report_provisioning_result`.

**Platform REST (admin monitor, `MANAGE_INTEGRATIONS`):**
| Method and path | Purpose |
|---|---|
| `GET /admin/integrations/tool-sync` | Page of organization × product sync status (filters) |
| `GET /admin/integrations/tool-sync/{organizationId}/{productId}` | One entry with recent deliveries and errors |
| `POST /admin/integrations/tool-sync/{organizationId}/{productId}/retry` | Retry failed deliveries |
| `POST /admin/integrations/tool-sync/{organizationId}/{productId}/replay` | Replay the current state (`aggregateTypes[]` optional) |

Errors: `retry` (temporary) and `rejected` (permanent) results as in the contract; REST uses the platform's usual 400/401/403/404/409.
