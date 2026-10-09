# API requirements — Platform ↔ tool synchronization (module `toolsync`)

Requirement: [REQ-INT-003](../../02-requirements/FRD/platform-tool-sync/requirement.md). Contract: [contract v1](../../09-integrations/platform-tool-contract-v1.md). Service clients: [deployment/keycloak-sync-clients.md](../../../deployment/keycloak-sync-clients.md).

## 1. MCP server for tools — `POST /api/mcp` (Streamable HTTP, JSON-RPC 2.0)
Bearer token of a tool's service client (client credentials). Every tool answers a **result object** (`status` applied / duplicate / rejected / retry, `version`, `reason`, `message`, `data`); a rule that is broken is `rejected`, never a protocol error. Who may call is decided per call (`ToolCallGuard`): the token's `azp` must be an ACTIVE `tool_connector.client_id` (`NOT_ALLOWED_CLIENT`); `tenantRef` must be a platform organization (`UNKNOWN_TENANT`); the tool must hold an ACTIVE subscription of that organization (`NO_ACTIVE_SUBSCRIPTION_FOR_TENANT`); changing calls also need a READY tenant (`TENANT_NOT_READY`).

| Tool | Arguments | What the platform does |
|---|---|---|
| `report_user_created` | `idempotencyKey`, `tenantRef`, `sub`, `email`, `emailVerified`, `firstName`, `lastName`, `phone?`, `nodeId?` | Links or creates the customer by `sub`; never merges by an unverified e-mail (`EMAIL_NOT_VERIFIED`); refuses an e-mail of another person (`INVALID_PAYLOAD`); checks the seat limit (`SEAT_LIMIT_EXCEEDED`); creates the membership (`MEMBER`, ACTIVE). **No product access.** `data.snapshots`: User, Membership |
| `update_user_profile` | `idempotencyKey`, `tenantRef`, `sub`, `changes{firstName,lastName,phone,attributes}`, `expectedVersion?` | Validates and commits; a lower `expectedVersion` → `STALE_VERSION` with the current snapshot; not a member → `NOT_A_MEMBER` |
| `create_org_node` / `update_org_node` / `move_org_node` / `delete_org_node` | `idempotencyKey`, `tenantRef`, node fields / `{id,newParentId}` / `{id}`, `expectedVersion?` | The hierarchy service's own rules (level order, unique sibling names); `CYCLE`, `NODE_IN_USE`, `STALE_VERSION`; the audit log names the tool as the actor |
| `get_entitlement` | `tenantRef`, `sub`, `productCode`, `action?` | `data {allowed, reason, endsAt?, productRole?}`; reasons `OK`, `NOT_PROVISIONED`, `ORG_INACTIVE`, `NOT_A_MEMBER`, `USER_INACTIVE`, `NO_SUBSCRIPTION`, `SUBSCRIPTION_ENDED`, `NO_PRODUCT_ACCESS`. Answered for any known organization even without an active subscription (the answer says so); `productCode` must be the caller's own |
| `report_provisioning_result` | `tenantRef`, `productCode`, `status` READY/FAILED, `schemaVersion?`, `error?`, `idempotencyKey?` | READY: tenant READY and a full send starts (once); FAILED: reason recorded; `UNKNOWN_TENANT` if the platform never asked |

Additive reason codes (contract stays at version 1): `SEAT_LIMIT_EXCEEDED`, `NOT_A_MEMBER`.

## 2. Platform → tool (outbound, MCP client)
`upsert_organization`, `upsert_org_node`, `delete_org_node`, `upsert_user`, `set_membership`, `set_subscription`, `set_user_access` (argument `envelope`), `provision_tenant` (`tenantRef`, `datasourceRef`, `organization`, `firstAdmin`, `schemaVersion?`), `get_state_digest`, `list_aggregate_versions`. The tool's answer is read as a result object; an unreachable tool or identity provider is a retry, never a failure of another tool.

## 3. Admin monitor — `/admin/events/tool-sync/**` (`MANAGE_INTEGRATIONS`, others 403; every action audited)
| Method | Path | Purpose | Errors |
|---|---|---|---|
| GET | `/overview` | Each tool with its tenants (status, schema version, last delivered, waiting and failed counts, last error) | 403 |
| GET | `/deliveries?status=&organizationId=&connectorId=&page=` | Messages, newest first, 20 per page | 400 (unknown status), 403 |
| POST | `/deliveries/{id}/retry` | A FAILED message back to PENDING with a fresh start (`TOOL_DELIVERY_RETRIED`) | 404, 409 (not FAILED) |
| POST | `/deliveries/{id}/replay` | The same object sent again as a new message with the current state (`TOOL_DELIVERY_REPLAYED`) | 404 |
| POST | `/connectors/{id}/pause`, `/resume` | Pause keeps changes as waiting messages (`TOOL_CONNECTOR_PAUSED/RESUMED`) | 404 |
| POST | `/tenants/{organizationId}/{connectorId}/start` | No tenant: request provisioning (needs an ACTIVE subscription); tenant READY: resync everything; FAILED: ask again (`TOOL_TENANT_STARTED`) | 400, 404 |
| POST | `/tenants/{organizationId}/{connectorId}/reconcile?repair=true` | Compare with the tool (`get_state_digest`) and resend what is missing (`TOOL_TENANT_RECONCILED`) | 404 |
