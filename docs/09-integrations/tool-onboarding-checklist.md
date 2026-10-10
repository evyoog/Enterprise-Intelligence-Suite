# Onboarding a product as a tool — checklist

[REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md), [contract v1](platform-tool-contract-v1.md), [phase plan](platform-tool-sync-plan.md) phase 9. The Thittam Macro Planner is the reference tool; copy its module, do not redesign it. One box per step; a step is done when its check passes.

## A. Decide
- [ ] The product code in the platform catalog (for example `valam`) and the product role names the platform will send (`<PREFIX>_ADMIN`, …).
- [ ] How the product stores a person and the hierarchy: what in the product plays the part of the organization, the hierarchy node, the user and the role. If it has no hierarchy, say so: nodes are then kept as data only.
- [ ] Where tenants live (a schema per organization in a shared database is the reference; other layouts are Q9, still open) and the datasource references the product's configuration will offer (`default`).

## B. In the product's repository
- [ ] Copy the `platformsync` module and `platform-connection.yml`; set `product-code`. All connection settings stay in that one file; secrets only from the environment.
- [ ] Implement `ToolAdapter` once: `applyOrganization`, `applyNode`, `deleteNode`, `applyUser`, `applyMembership`, `applyAccess` (role map), `migrateSchema`, `grantInitialAdmin`, `retireSeedAccounts`, and `projectedVersions` for the types it stores in its own tables (reconcile reads the rows, not the inbox).
- [ ] The product's own migrations for the sync columns (`platform_ref`, `platform_version` on the node and user tables) and the generic tables (inbox, aggregate version, organization, subscription, user access, sync error).
- [ ] Never create the product's business records from a synchronization message (BR-SYN-010).
- [ ] The MCP server serves the tools of contract section 4 (including `get_state_digest` and `list_aggregate_versions`) and checks the caller's `azp` against `platform.inbound.allowed-clients`.
- [ ] Access checks: the entitlement filter in front of every authenticated request (managed tenants), fail-closed for sensitive actions.
- [ ] Copy the contract file unchanged into the repository's docs.

## C. Keycloak ([guide](../../deployment/keycloak-sync-clients.md))
- [ ] Service client `<tool>-sync` with an Audience mapper for the platform's client; `eis-sync` gets an Audience mapper for the tool's client.
- [ ] Secrets in the secrets manager; `PLATFORM_SYNC_CLIENT_SECRET` set on the tool.

## D. In the platform
- [ ] A `tool_connector` row (product id, code, MCP URL, client id).
- [ ] The product's plans carry the product code; an ORG_ADMIN with a Keycloak id exists for a pilot organization.

## E. Prove it (all must pass before a customer is switched on)
- [ ] The tool's unit tests with the platform simulator: contract section 8 apply rule, out-of-order versions, hierarchy rules, wrong client, wrong tenant, `provision_tenant` twice.
- [ ] A real database run of the same, and a tenant-spoofing test (a tenant is chosen only by the platform organization id in the registry).
- [ ] The end-to-end scenarios of `PlatformMacroEndToEndTest` adapted to the product: provision and fully synchronize; add and move a node; a person with access appears with the mapped role and node; an edit inside the tool is saved on the platform first; revoked access and an ended subscription are denied; the tool down then up; a deleted row repaired by reconcile.
- [ ] Metrics visible under the same names ([operations](platform-tool-operations.md)); alerts wired.
- [ ] Adoption of existing tenants, if any: dry run, review, apply; the tenant is switched to platform-managed only after the platform has delivered access for everyone.
