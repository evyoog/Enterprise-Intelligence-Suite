# Platform ↔ Tool synchronization — phase plan

**Status:** Proposed (2026-10-09). **Phase 0 written, Phases 1–4 built on 2026-10-09** (see section 6). Phases 1 and 2 are merged to Macro `main` (PRs #20 and #21); phases 3 and 4 are on one Macro branch (`claude/dazzling-meitner-y26nsv`), not yet merged. REQ-INT-003 **Approved 2026-10-09**. Phases 5–9 are not built.
**Owner:** Product owner. **Written for:** any engineer or AI agent who has not seen the conversation behind it.
**Scope:** EIS platform (this repo, `evyoog/Enterprise-Intelligence-Suite`) and the first tool, Thittam Macro Planner (repo `evyoog/evyoog-thittam-macro`, the "Vyoog PMS" app). Valam and later tools reuse the same module and contract.

Read sections 1 to 5 before any phase. Every phase section is written to be executed on its own.

---

## 0. Glossary

| Term | Meaning |
|---|---|
| **Platform / EIS** | This repo. The hub: organizations, users, hierarchy, subscriptions, product access. Spring Boot 3.2.5, Java 21, PostgreSQL schema `eis_platform`, Keycloak realm `eVyoog`. |
| **Tool** | A product (Thittam, Valam, Varthan…) in its own repo with its own schemas. First tool: Thittam Macro Planner (`com.vyoog.pms`). |
| **Tenant** | One customer organization as seen by a tool. Today in Thittam: one PostgreSQL schema. Target: one database per client, one schema per app. |
| **Tenant registry** | A table in the tool that maps a tenant to its datasource and schema. Replaces the static `app.tenants` list. |
| **Projection** | A tool-side copy of centrally owned data (organization, users, hierarchy, subscription). Read-only for tool code except through the sync module. |
| **Single writer** | Centrally owned fields are changed only by the platform. A tool that wants to change one calls the platform, then applies the answer. |
| **Aggregate** | One synchronized thing: an organization, an org node, a user, a membership, a subscription, a product-access grant. |
| **Version** | A number on each aggregate that only goes up. A tool ignores any message whose version is not greater than the one it holds. |
| **Outbox** | A table written in the same database transaction as the change. A background job delivers its rows. Guarantees "change and event" are never half done. (EIS already has one: `outbox_event`, `event_handler_receipt`.) |
| **Inbox** | The mirror on the receiving side: a table of event ids already applied. Makes delivery idempotent. |
| **Idempotent** | Applying the same message twice has the same effect as once. |
| **Reconcile** | A periodic comparison of platform state with tool state that repairs any drift. Safety net for lost or reordered messages. |
| **MCP** | Model Context Protocol: JSON-RPC 2.0 over HTTP (Streamable HTTP). Used here only as the transport for backend-to-backend calls (decision Q1). |
| **azp** | The Keycloak token claim naming the client that obtained the token. Used to allow-list callers. |
| **Bridge (SSO bridge)** | EIS's cookie-based cross-app login: a `vyoog_sso` cookie holds a random bridge-session id; apps redeem it backend-to-backend. |

---

## 1. Decisions locked (answers of 2026-10-09)

| # | Decision | Notes for implementers |
|---|---|---|
| Q1 | **MCP is the transport; reliability comes from the platform** (outbox, retries, idempotency keys, reconcile). | Do not rely on MCP for delivery guarantees. |
| Q2 | **Full hierarchy sync**: nodes, levels, moves, deletes, and each user's node placement. | |
| Q3 | **Tools store everything the platform has** about an organization, because the platform will add fields later. | See assumption A1: this is built as a generic snapshot so new fields need no tool migration. |
| Q4 | **Cross-product user key = Keycloak user id (`sub`).** | Thittam already uses it as `users.id`. |
| Q5 | **Match by Keycloak id.** A pre-existing local row may be linked by email only if the Keycloak email is verified, and every link is logged. | Thittam's current "adopt by email" code must be changed (Phase 4). |
| Q6 | **Tools may create Keycloak users directly, then report to the platform.** | See assumption A2 for the guard rails that make this safe. |
| Q7 | **Runtime tenant registry + routing datasource.** Database per client, schema per app. The host no longer decides the tenant; it is checked against the registry and the token. | |
| Q8 | **Individuals are one-person "personal organizations"** using the same tenant machinery. | |
| Q9 | **Hosting and scale: decided later.** | The registry stores a `connection_ref`, never a hard-coded host. Existing tenants keep working unchanged. |
| Q10 | **Platform stores the member's product role** (`organization_product_access.product_role`); the tool maps it to its own roles and may refine inside the tool. | |
| Q11 | **Approve REQ-TEN-005 as drafted.** Subscription expiry means immediate denial, no trial or grace for now. **Expiry is stored as end of day: `YYYY-MM-DD 23:59:00.000 +0530`.** | See assumption A3. |
| Q12 | **Hybrid entitlements:** local copy for normal requests, central check for sensitive actions, revoke within about 5 minutes (bounded by token lifetime). | |
| Q13 | **Keep the SSO bridge**; add one Keycloak service client per app for sync calls; retire the single shared secret later. | |
| Q14 | **FRDs first, approved, then build in sprints per repo.** | Phase 0. |
| Order | **Tool (Macro) first, platform later; the same module and structure for every tool; all connection settings in one file per tool.** | See assumptions A4 and A5. |
| SSO | **Macro has no SSO bridge code today; implement it.** | Phase 4. |

### Assumptions to confirm (I made these; change them if wrong)
- **A1 — "everything the platform has".** Applied to organization and user profile data as an *open snapshot*: fixed columns for the stable fields plus a JSONB `attributes` column that stores every other field the platform sends. New platform fields then need no tool migration. **Never synchronized, whatever the rule:** passwords, tokens, MFA secrets, payment or bank details, invoices, internal notes.
- **A2 — tool-created users (Q6 B).** A tool may create a Keycloak user only if it (a) first looks the person up in Keycloak by email and reuses the existing `sub` if found, (b) sets the Keycloak **email to the real email** (not to the username — Thittam does that today), (c) reports the new user to the platform with `report_user_created` using an idempotency key, and (d) treats the platform's reply as the authoritative record from then on. Without (a) and (b), one person gets two Keycloak users or a realm-wide email clash.
- **A3 — expiry format.** The subscription end is stored as a `timestamptz` instant equal to 23:59:00.000 on the end date in Asia/Kolkata (+05:30), and is written in contracts as `2026-10-31T23:59:00.000+05:30`. Seconds are `:00`, not `:59` — exactly as you wrote it.
- **A4 — order.** "Connect Macro first, platform later" is read as: build and test the **tool side first** against a platform simulator (Phase 1–6), then build the platform side (Phase 7), then connect them (Phase 8). Say so if you want the platform first.
- **A5 — "connection metrics in one file".** Read as: all connection **settings** (URLs, client ids, tenant registry source, timeouts, retry, TTLs, contract version) live in **one config file per tool**, and the sync metrics (counters, timers) are defined in one class.
- **A6 — single writer.** Edits to centrally owned fields made inside a tool go through the platform and are applied from its answer. This is how "edit anywhere, saved everywhere" works without conflicts.

---

## 2. Current state (verified by reading the code, 2026-10-09)

### 2.1 Thittam Macro Planner — `evyoog/evyoog-thittam-macro`
| Area | Fact | Where |
|---|---|---|
| Stack | Spring Boot 3.2.5, Java 21, Maven, PostgreSQL, React 18 + Vite (JS), Flyway, MapStruct, context path `/api`, port 8080 | `backend/pom.xml`, `application.yml` |
| Tenancy | One PostgreSQL schema per customer in one shared database. Schema chosen per request by `TenantHostFilter` from `Origin`/`Referer`/`Host` through the static map `app.tenant-hosts`. Allow-list `app.tenants`. Hibernate multi-tenancy with `MultiTenantSchemaConnectionProvider` (`connection.setSchema(tenant)`). | `config/TenantHostFilter`, `CurrentTenantResolver`, `MultiTenantSchemaConnectionProvider`, `MultiTenantHibernateConfig` |
| Migrations | `TenantFlywayConfig` runs Flyway once per listed schema at startup; schemas are never created automatically; scripts use `"${flyway:defaultSchema}"`. Latest migration **V74**. | `config/TenantFlywayConfig`, `db/migration` |
| Organization | **No organization table.** The tenant is the schema. Root of `org_nodes` is the organization; `app_settings` (single row) holds the profile. | V47, V62 |
| Hierarchy | `org_nodes(id UUID, parent_id, name, type, code, description, sort_order, is_active)`, `org_level_config(type, level_rank)` (ORGANIZATION, DIVISION, BUSINESS_UNIT, DEPARTMENT, LOCATION, COST_CENTER, TEAM), `org_node_history`, `users.org_node_id`, `role_assignments`, `groups`. ADMIN-only endpoints `/admin/org-nodes`. | V47, V52, V56, V57, `OrgNodeService` |
| Users | `users.id` = Keycloak `sub`. Roles `ADMIN`, `PROJECT_OWNER`, `ACTIVITY_USER`, `TASK_USER` (client roles in Keycloak, mirrored in `roles`/`user_roles`). `UserService.createUser` creates the Keycloak user itself (with a `tenant` attribute, email field set to the username). | `entity/User`, `service/UserService`, `service/KeycloakAdminService` |
| First login | `UserProvisioningFilter` creates a local user from the JWT for **any** valid realm token, in whatever tenant the host selects, and adopts a pre-seeded row by email. | `security/UserProvisioningFilter`, `UserIdentityReconciliationService` |
| Login | The browser asks Keycloak directly (password grant, `keycloakLogin`); optional federated login (`sso_enabled`, `kc_idp_hint`). **No EIS SSO bridge code.** | `frontend/src/api/auth.api.js`, V65 |
| Existing integration pattern | `/api/mcp` (MCP server): caller's Keycloak client (`azp`) must be in `app.mcp.allowed-clients`; tenant from `Host`; caller acts as ADMIN; service accounts never get a user row. Macro also calls Agile's MCP server. | `mcp/McpServerConfig`, `McpCaller`, docs `INT-agile-planner-mcp.md` |

### 2.2 EIS platform — this repo
| Area | Fact | Where |
|---|---|---|
| Organization | `organization` (id BIGINT, code, name, lifecycle/registration status, licensed seats…). Hierarchy `org_node` (BIGINT ids, parent, type, code, sort_order, active), level config, `organization_member.org_node_id`. REQ-TEN-006/007/008. | modules `registration`, `orgdirectory`, `invitation` |
| Identity | `customer.keycloak_sub`; Keycloak user creation through `KeycloakAdminClient.createUser` (real email). | modules `auth`, `registration` |
| Subscription | `product_subscription` (owner org or customer, product, plan, status, `started_at`, **`expires_at` as UTC `Instant`**, auto-renew, quantity/seats). | `ProductSubscription` |
| Product access | **`organization_product_access`**: member + product + `product_role` string (e.g. `PMS_ADMIN`) + status. "Being a member never implies this." | `OrganizationProductAccess` |
| Events | Outbox (`outbox_event`) with in-process handlers (`PlatformEventHandler`), per-handler receipts, admin page `/admin/integrations/events`. Event types today: checkout, order, subscription lifecycle, invoice, payment, seats, renewal reminder. **No** user, organization, node or product-access events. | `integration` module, `PlatformEventTypes` |
| SSO bridge | Cookie `vyoog_sso` (domain from `SSO_COOKIE_DOMAIN`) holds a bridge-session id; table `sso_bridge_session` keeps the Keycloak refresh token server-side. Backend-to-backend `POST {partner}/internal/sso/token` and `/internal/sso/logout`, header `X-Internal-Sso-Secret` (one shared secret). Partner list `VYOOG_SSO_PARTNER_BACKEND_URLS` already points at `localhost:8080/api` (Macro's port and path). | `SsoBridgeSessionService`, `InternalSsoClient`, `InternalSsoController`, `SessionCookieService` |
| Process rules | Approved FRD before code; migration in `database/migrations/` and `schema.sql`; test cases in `test-cases/`; layering controller → service → repository (`LayeredArchitectureTest`). | `CLAUDE.md` |

### 2.3 Gaps this plan closes
1. Thittam cannot create tenants at runtime; tenant choice trusts a caller-supplied header.
2. Thittam has no place for organization profile, subscription, platform references, or entitlements.
3. Thittam's first-login code provisions any realm user into any tenant, and adopts rows by email.
4. Thittam and EIS both create Keycloak users with different conventions.
5. IDs differ (UUID vs BIGINT); no idempotent mapping exists.
6. EIS has no user/organization/node/access events, no per-aggregate version numbers, and no delivery to tools.
7. Thittam is not a participant in the SSO bridge.
8. Subscription expiry is a UTC instant at an arbitrary time of day.

---

## 3. Target architecture

```
                    Keycloak (realm eVyoog)  ── one user per person, key = sub
                           ▲   ▲
        register / invite  │   │ tokens
                           │   │
┌──────────────────────────┴───┴───────────────┐        ┌───────────────────────────────┐
│ EIS PLATFORM (hub, single writer)            │        │ TOOL (e.g. Thittam)           │
│  platform DB: organization, org_node,        │ events │  platformsync module          │
│  customer, member, subscription,             │───────►│   inbound MCP server  (apply) │
│  product_access, tenant registry             │  MCP   │   outbound MCP client (ask)   │
│  outbox → delivery → retries → receipts      │◄───────│   inbox, entitlement cache    │
│  platform MCP server (tools for tools)       │ calls  │  tenant registry + routing DS │
│  reconcile job, admin sync monitor           │        │  schema per tenant:           │
└──────────────────────────────────────────────┘        │   projections + product data  │
                                                        └───────────────────────────────┘
```

### 3.1 Data ownership
| Data | Owner | In the tool |
|---|---|---|
| Organization profile, status | Platform | Projection (`platform_organization`, columns + `attributes` JSONB) |
| Org hierarchy, levels | Platform | Projection into the tool's own node table, `platform_ref` = platform id |
| User identity & profile (name, email, status, phone…) | Platform (Keycloak holds credentials) | Projection in `users` (id = `sub`), non-credential fields only |
| Membership, node placement | Platform | `users.org_node_id` |
| Subscription (plan, status, effective dates, seats) | Platform | `platform_subscription` |
| Product access and **product role** | Platform | `platform_user_access`; mapped to the tool's roles |
| Tool roles beyond the mapping, projects, tasks, etc. | Tool | Native tables |
| Passwords, MFA secrets | Keycloak | Never copied |

### 3.2 Flows
**F1 Platform creates or changes something (fan-out).** Service writes the change and an outbox row in one transaction → delivery job picks rows for every tool that has an active subscription for the organization → calls the tool's MCP tool (`upsert_*`) → tool checks the inbox and version, applies, replies `applied` → platform records a receipt per tool. Failure → retry with backoff → after the limit status `FAILED`, visible in the admin monitor, replayable.

**F2 A user is edited inside a tool (write-through).** The tool's screen calls its local service → the service calls the platform MCP tool (`update_user_profile`, `move_org_node`, …) with an idempotency key → the platform validates, commits, bumps the version, queues fan-out to **all** tools, and replies with the new snapshot → the tool applies that snapshot at once (same code path as F1). If the platform is unreachable the edit is refused with "try again later"; nothing is written locally.

**F3 A user registers in a tool (Q6 B).** The tool looks the email up in Keycloak. Found → reuse `sub`. Not found → create the user (email = real email) → `report_user_created(sub, email, name, tenantRef, idempotencyKey)` → the platform creates or links customer and membership by `sub`, answers with the canonical snapshot → the tool stores its row from that answer. The user has **no product access** until it is granted; registration never creates business records.

**F4 Subscription or access changes.** `SubscriptionActivated/Changed/Suspended/Cancelled/Expired`, `UserProductAccessGranted/Revoked` are fanned out like F1. The tool updates its entitlement tables and invalidates its cache.

**F5 Browser launch (SSO).** A person signed in to EIS opens the tool; the tool's backend redeems the `vyoog_sso` bridge session at EIS and obtains a token for the tool's own Keycloak client (Phase 4).

**F6 Request authorization in the tool.** Token valid → tenant resolved from registry → user active in this tenant → organization active → subscription effective → user has product access → product provisioned → tool permission for the action (Phase 5).

---

## 4. The contract (version 1)

The same contract is implemented by every tool. Put it in `docs/09-integrations/platform-tool-contract-v1.md` in the platform repo (Phase 0) and copy it into each tool repo.

### 4.1 Envelope for every message
```json
{
  "contractVersion": "1",
  "eventId": "7c1f…uuid",
  "eventType": "OrgNodeMoved",
  "occurredAt": "2026-10-09T10:15:30.000+05:30",
  "aggregateType": "OrgNode",
  "aggregateId": "4812",
  "version": 17,
  "tenantRef": "org-100",
  "payload": { }
}
```
- `tenantRef` is the platform organization id as a string. The tool resolves it through its tenant registry; it **never** accepts a database or schema name from a message.
- `version` is per aggregate and strictly increasing. The tool applies a message only if `version` is greater than its stored `last_version` for that aggregate; equal or lower is acknowledged as `duplicate` and ignored.
- All timestamps are ISO-8601 with an offset. Subscription effective dates use A3: `2026-10-31T23:59:00.000+05:30`.

### 4.2 Platform → tool MCP tools (served by the tool at `/api/mcp`)
| Tool | Arguments | Effect in the tool |
|---|---|---|
| `provision_tenant` | `tenantRef`, `organization` snapshot, `adminUser` snapshot, `schemaVersion` | Resolve or create the tenant's schema and migrate it; create the organization root; return `{status, schemaVersion}`. Idempotent. |
| `upsert_organization` | envelope with organization snapshot | Insert or update `platform_organization`, keep the root node name in step. |
| `upsert_org_node` | envelope with node snapshot (`id`, `parentId`, `name`, `type`, `code`, `description`, `sortOrder`, `active`) | Create or update by `platform_ref`; reject a move that creates a cycle; return `applied`/`duplicate`. |
| `delete_org_node` | envelope | Delete if no children/users; else `rejected` with reason (reconcile will flag it). |
| `upsert_user` | envelope with user snapshot (`sub`, `email`, `firstName`, `lastName`, `phone`, `status`, `nodeId`, `attributes`) | Upsert `users` by `sub`. **Never** creates business records. |
| `set_membership` | envelope (`sub`, `status`: ACTIVE/SUSPENDED/REMOVED) | Activate/deactivate the local user. |
| `set_subscription` | envelope (`productCode`, `plan`, `status`, `startsAt`, `endsAt`, `seats`) | Upsert `platform_subscription`. |
| `set_user_access` | envelope (`sub`, `productCode`, `productRole`, `status`) | Upsert `platform_user_access`; map the role. |
| `get_state_digest` | `tenantRef`, `aggregateTypes[]` | Return per aggregate type: count and a hash over `(id, version)` — used by reconcile. |
| `list_aggregate_versions` | `tenantRef`, `aggregateType` | Return `(id, version)` pairs so the platform can resend what is missing. |

### 4.3 Tool → platform MCP tools (served by the platform at `/api/mcp`)
| Tool | Arguments | Platform effect |
|---|---|---|
| `report_user_created` | `idempotencyKey`, `sub`, `email`, `firstName`, `lastName`, `tenantRef`, `nodeId?` | Link or create customer + membership by `sub`; never merge by unverified email; return snapshot. |
| `update_user_profile` | `idempotencyKey`, `sub`, changed fields | Validate, commit, fan out, return snapshot. |
| `create_org_node` / `update_org_node` / `move_org_node` / `delete_org_node` | `idempotencyKey`, fields | Same rules as the platform's hierarchy service; return snapshot. |
| `get_entitlement` | `sub`, `tenantRef`, `productCode` | Authoritative answer for sensitive actions: `{allowed, reason, subscriptionEndsAt, productRole}`. |
| `report_provisioning_result` | `tenantRef`, `productCode`, `status`, `schemaVersion`, `error?` | Record on the tenant registry. |

### 4.4 Results and errors
Every tool returns structured JSON: `{ "status": "applied" | "duplicate" | "rejected" | "retry", "version": 17, "reason": "…" }`. `retry` means a temporary problem (the caller retries with backoff); `rejected` is permanent (the caller stops and records it). Unknown errors count as `retry` up to the limit.

### 4.5 Authentication and authorization
- Each tool has its own Keycloak **service client** (client-credentials), e.g. `thittam-sync`; the platform has `eis-sync`. Tokens are validated for signature, issuer, expiry.
- The receiver checks the caller's `azp` against its allow-list (`platform.inbound.allowed-clients`), then checks scope: the platform confirms the calling tool has an **active subscription** for the `tenantRef` in the call; a tool confirms the caller is the platform.
- Secrets only from environment variables or a secrets manager; none committed.

### 4.6 Time rule (Q11 / A3)
`endsAt = (end date at 23:59:00.000, zone Asia/Kolkata)`. A subscription is effective while `startsAt <= now <= endsAt`; after `endsAt` access is denied at once (no grace). Stored as `timestamptz`; contracts carry `+05:30`.

---

## 5. The standard tool module ("same structure in every tool")

Every tool gets one self-contained package (Java: `com.vyoog.<tool>.platformsync`; names below use Thittam's `com.vyoog.pms`). Same class names, same tables, same config file. Only `productCode` and the **adapter** differ.

```
platformsync/
  config/PlatformConnectionProperties.java   ← binds the single config file (section 5.1)
  config/PlatformSyncConfig.java             ← wires beans, scheduling, MCP servlet
  tenant/TenantRegistry.java                 ← lookup + cache of tenants (DB table)
  tenant/TenantRoutingConnectionProvider.java← replaces MultiTenantSchemaConnectionProvider
  tenant/TenantProvisioner.java              ← create schema/db, run Flyway, record version
  inbound/PlatformMcpTools.java              ← the section 4.2 tools (@Tool methods)
  inbound/PlatformCallerGuard.java           ← azp allow-list, like McpCaller
  inbound/InboxService.java                  ← idempotency + version check
  outbound/PlatformMcpClient.java            ← section 4.3 calls, client-credentials token
  outbound/WriteThroughService.java          ← F2/F3 helpers
  apply/OrganizationApplier.java, OrgNodeApplier.java, UserApplier.java,
        SubscriptionApplier.java, AccessApplier.java
  entitlement/EntitlementService.java        ← cache (TTL) + central check
  entitlement/EntitlementFilter.java         ← request filter (Phase 5)
  sso/…                                       ← bridge (Phase 4)
  reconcile/ReconcileService.java            ← digest compare + repair
  metrics/PlatformSyncMetrics.java           ← Micrometer counters and timers
  adapter/ToolAdapter.java                   ← the ONLY tool-specific interface (below)
```

`ToolAdapter` is the extension point. Each tool implements it once:
```java
public interface ToolAdapter {
    String productCode();                                   // e.g. "thittam"
    Role mapProductRole(String platformProductRole);        // PMS_ADMIN -> ADMIN, ...
    void applyOrganization(OrganizationSnapshot s);         // tool tables for the org profile
    void applyNode(OrgNodeSnapshot s);                      // tool's own hierarchy table
    void applyUser(UserSnapshot s);                         // tool's user table (no business records!)
    void deactivateUser(String sub);
    boolean hasBusinessRecords(String sub);                 // used before hard delete
    void migrateSchema(TenantTarget target);                // run this tool's Flyway
}
```

### 5.1 The one connection file
`backend/src/main/resources/platform-connection.yml`, imported with `spring.config.import: classpath:platform-connection.yml`. Same keys in every tool. Secrets are `${ENV_VAR}` with **no default**.

```yaml
platform:
  contract-version: "1"
  product-code: thittam                      # the only per-tool identity value
  hub:
    mcp-url: ${EIS_PLATFORM_MCP_URL}          # e.g. https://eis.evyoog.com/api/mcp
    token-url: ${KEYCLOAK_TOKEN_URL}
    client-id: thittam-sync
    client-secret: ${PLATFORM_SYNC_CLIENT_SECRET}
    timeout-seconds: 5
  inbound:
    allowed-clients: eis-sync                 # azp allow-list
  tenants:
    source: db                                # db | file (file = local dev only)
    control-schema: platform_control
    cache-seconds: 60
  entitlement:
    cache-ttl-seconds: 300                    # Q12: revocation within ~5 min
    sensitive-actions-central-check: true
    fail-closed: true
  retry:
    max-attempts: 8
    base-seconds: 5
    max-backoff-seconds: 900
  reconcile:
    cron: "0 */30 * * * *"
  sso:
    enabled: true
    cookie-name: vyoog_sso
    cookie-domain: ${SSO_COOKIE_DOMAIN}
    partner-backend-urls: ${VYOOG_SSO_PARTNER_BACKEND_URLS}
    shared-secret: ${INTERNAL_SSO_SHARED_SECRET}   # retired later (Q13)
    keycloak-client-id: ${KEYCLOAK_PMS_CLIENT_ID}
  metrics:
    enabled: true
    prefix: platformsync
```
Rules: no value is hard-coded in code; every tool's copy of this file differs only in `product-code`, client id, and environment variables.

### 5.2 Standard tables (Flyway, in the tool's tenant schema unless stated)
| Table | Purpose |
|---|---|
| `platform_tenant` (**control schema**, one per database cluster) | `tenant_ref`, `host`, `datasource_ref`, `schema_name`, `status` (PENDING/READY/FAILED/SUSPENDED), `schema_version`, `last_error`, `updated_at` |
| `platform_organization` | `platform_org_id`, `name`, `status`, `attributes JSONB`, `version`, `updated_at` |
| `platform_subscription` | `product_code`, `plan`, `status`, `starts_at`, `ends_at`, `seats`, `version` |
| `platform_user_access` | `sub`, `product_code`, `product_role`, `status`, `version` |
| `platform_inbox` | `event_id` (PK), `aggregate_type`, `aggregate_id`, `version`, `received_at`, `result` |
| `platform_aggregate_version` | `(aggregate_type, aggregate_id)` PK, `last_version`, `deleted` — the version guard |
| `platform_link_log` | every email-based link: `sub`, `local_user_id`, `email`, `reason`, `linked_at` (Q5) |
| `platform_sync_error` | rejected or failed messages for the monitor |
| Added columns | `platform_ref` (text) and `version` on the tool's hierarchy and user tables |

---

## 6. Phases

Order (A4): **0 contract → 1–6 Macro (tool side, against a simulator) → 7 platform → 8 connect → 9 roll out**. Each phase is a branch + pull request into `dev` of the named repo and ends with the stated acceptance tests. Do not start a phase before the previous one is merged, except where a phase says it can run in parallel.

---

### Phase 0 — Contract, requirements, approvals  (repo: EIS, documents only)
**Goal.** Make everything above an approved requirement so code can follow the project's own rules.
**Why.** `CLAUDE.md` forbids building anything without an Approved FRD, and the contract is the thing every tool depends on.
**What.**
1. `docs/02-requirements/FRD/platform-tool-sync/` (new, copy `_template`): requirement, business rules, workflow, UI (admin monitor), API (the section 4 contract), acceptance criteria. Requirement id proposal: `REQ-INT-003`.
2. Update `provisioning-contract` (REQ-ORD-002): answer its open questions 1, 4, 6 (delivery = MCP transport with outbox; fields = section 4; secrets = per-tool service clients).
3. Mark `access-management` (REQ-TEN-005) **Approved** per Q11.
4. New business rule for expiry (A3): in `docs/03-business-rules/` and `subscription-lifecycle` FRD.
5. `docs/09-integrations/platform-tool-contract-v1.md` (section 4 verbatim, with JSON schemas).
6. Decision record C86 in `open-decisions.md` (Q1–Q14, assumptions A1–A6 with your confirmation), sprint page rows, test-case ids reserved.
**How.** Markdown only. Mermaid sequence diagrams for F1–F6.
**Done on 2026-10-09 (written, waiting for approval):** [REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md) with its five companion files; [contract v1](platform-tool-contract-v1.md); [BR-INT-001](../03-business-rules/BR-INT-001-single-writer-and-tenant-scope.md) and [BR-SUB-010](../03-business-rules/BR-SUB-010-subscription-end-time.md); [REQ-TEN-005](../02-requirements/FRD/access-management/requirement.md) Approved with defaults for its questions 3 and 7; [REQ-ORD-002](../02-requirements/FRD/provisioning-contract/requirement.md) answers; subscription-lifecycle amendment; [decision C86](../01-business/roadmap/open-decisions.md#c86); [screen spec](../05-ui/screen-requirements/tool-sync-monitor.md); [test plan](../../test-cases/functional/platform-tool-sync/TESTPLAN-INT-003.md).
**Acceptance.** FRD status Approved by the product owner; contract file reviewed; assumptions A1–A6 confirmed or changed.
**Tests.** Link check; no code.
**Rollback.** Revert the docs commit.

---

### Phase 1 — Macro: module skeleton, connection file, tenant registry and routing  (repo: Macro)
**Goal.** Make Macro able to resolve tenants from a database table and route to a datasource and schema at run time, without changing behaviour for today's two tenants.
**Why.** Everything else needs a runtime tenant list (Q7). Today `app.tenants` is static and the schema is chosen by a header the caller controls.
**What.**
1. Create the `platformsync` package skeleton (section 5), the single `platform-connection.yml` and `PlatformConnectionProperties`.
2. Control schema `platform_control` with `platform_tenant` (Flyway migration **V75**, run once against the default datasource).
3. `TenantRegistry`: loads rows, caches `cache-seconds`, exposes `resolveByHost(host)`, `resolveByRef(tenantRef)`, `all()`.
4. `TenantRoutingConnectionProvider` (replaces `MultiTenantSchemaConnectionProvider` in `MultiTenantHibernateConfig`): `getConnection(tenantId)` → `registry.datasource(tenantId).getConnection()` then `connection.setSchema(registry.schema(tenantId))`. Per-datasource Hikari pools are created lazily and bounded (`maximum-pool-size` small, `minimum-idle` 0).
5. `CurrentTenantResolver` reads the registry instead of `app.tenants`; `TenantHostFilter` stays but only **proposes** a tenant; the final tenant must also match the token (Phase 5 enforces it).
6. `TenantFlywayConfig` iterates the registry rows with status READY instead of `app.tenants`.
7. Seed `platform_tenant` from today's config: `vyoog_pms` (host `demopms.evyoog.com`) and `vyoog_pms_vyoog` (host `pms.evyoog.com`), same shared datasource, same schemas. **No data moves.**
8. Keep the old properties as a fallback for one release, logging a deprecation warning.
**How.** Hibernate `MultiTenantConnectionProvider` is the only seam; `datasource_ref` is a key into a `Map<String, DataSource>` built from environment-supplied connection settings (never a URL in the table, never from a request).
**Built on 2026-10-09** in `evyoog/evyoog-thittam-macro`, branch `platform-sync/phase-1-tenant-registry` (merged to Macro `main` on 2026-10-09 at the product owner's request). Started on the product owner's instruction "start phase 1"; REQ-INT-003 was approved the same day.

**What was built, and how it differs from the plan above**
| Plan said | What was built | Why |
|---|---|---|
| Migration **V75** creates the control schema | `db/control/V1__create_platform_tenant.sql`, a **separate Flyway location** with its own history table in the control schema | The registry has to exist before anyone knows which tenant schemas to migrate (chicken-and-egg with `db/migration`). `TenantFlywayConfig` still runs it, so there is still one mechanism. |
| One table `platform_tenant` with a `host` column | Two tables: `platform_tenant` and `platform_tenant_host` | A tenant can be reached by several hostnames (the seed already has `pms.evyoog.com` and `localhost` for one tenant). |
| `platform.platform.*` keys for the hub | `platform.hub.*` | The doubled name was confusing. Section 5.1 is updated. |
| "Requests cannot choose a schema through any header" | The tenant value must be the **reference of a READY registry tenant**; a schema name is accepted only if it is registered. The JWT-claim and `X-TENANT-ID` sources stay **on** (`platform.tenants.fallback.*`) and are switchable | Turning them off now would break clients that rely on them today and the plan says phase 1 changes no behaviour. Phase 5 turns them off after membership is checked. |
| "An unknown host is refused" | A host of a non-READY tenant is refused (403). An **unknown** host still falls back to the default tenant unless `refuse-unmapped-host=true` | Same reason; the health check and unauthenticated paths use unmapped hosts. |
| Seed the table from config once | Seed **missing** rows at every start (never changes existing rows) | Keeps the old "add to `APP_TENANTS`" workflow working while a suspension made in the table survives restarts. |
| — | `platform.tenants.source=file` keeps the legacy lists as the whole registry | Local development and the test profile never touch the control schema. |

**Verified:** 450 existing + 42 new unit tests pass (492, 6 skipped = the opt-in PostgreSQL test); `TenantRegistryPostgresTest` (6 tests, real PostgreSQL 16) passes, including a full tenant migration (≥ 74 migrations) through the registry; the whole application started on PostgreSQL with two tenants, routed requests by `Origin` to the right schema, reached a third tenant added to the table at run time within the cache time, and answered 403 for it once suspended. A real boot also found one defect the unit tests could not (a missing `@Autowired` on the registry's constructor), now fixed.

**Not covered by phase 1 (still true, scheduled):** the `X-TENANT-ID` and token-claim sources only act on authenticated requests, so they could not be exercised without Keycloak (unit tests cover the logic); `@Scheduled` jobs still run in the default tenant only; `TenantFrontendUrl` is still keyed by the legacy list.

**Acceptance.** The app boots with the registry and behaves exactly as before for both tenants; adding a registry row and calling `registry.refresh()` makes a third tenant resolvable without a restart; an unknown or `SUSPENDED` host is refused; requests cannot choose a schema through any header.
**Tests.** Unit: registry cache/refresh, resolver refusal. Integration (local Postgres): two schemas isolated; pool bounded; Flyway runs per READY row. Regression: existing unit suite (`mvn test -Dtest='!VyoogPmsApplicationTests'`).
**Rollback.** Switch `platform.tenants.source` to `file` to use the old properties; revert V75 is not needed (additive).

---

### Phase 2 — Macro: data model and inbound sync  (repo: Macro)
**Goal.** Macro can receive and apply organization, hierarchy, user, membership, subscription and access data idempotently.
**Why.** This is the receiving half of goals "same hierarchy in the tool" and "organization and subscription details stored in each tool".
**What.**
1. Migrations **V76–V79** (additive, `"${flyway:defaultSchema}"`): the section 5.2 tables; `platform_ref` and `version` columns on `org_nodes` and `users`; unique index on `org_nodes.platform_ref`; `org_level_config` is **not** overwritten, only extended (new types from the platform are inserted with their rank).
2. Inbound MCP tools (section 4.2) in `PlatformMcpTools`, protected by `PlatformCallerGuard` (allow-list on `azp`, `tenantRef` resolved through the registry, caller acts as a system principal, no user row).
3. `InboxService.apply(envelope, handler)`: in **one transaction** — if `event_id` is in `platform_inbox` return `duplicate`; if `version <= last_version` return `duplicate`; else call the handler, store inbox + new `last_version`, return `applied`.
4. Appliers use the existing services where they exist (reuse `OrgNodeService` validation: no cycles, level order) but bypass the single-writer UI block.
5. Hierarchy rules: nodes arrive in any order; an `upsert_org_node` whose parent is unknown is stored as `PENDING_PARENT` and retried when the parent arrives (reconcile also repairs it). Moves write `org_node_history`.
6. Users: `upsert_user` finds by `sub`; if no row and an unlinked row has the same **verified** email, link it and write `platform_link_log` (Q5); otherwise insert. It never creates projects, tasks, teams or allocations.
7. Role mapping: `ToolAdapter.mapProductRole`. Initial mapping for Thittam, **to confirm**: `PMS_ADMIN→ADMIN`, `PMS_MANAGER→PROJECT_OWNER`, `PMS_USER→TASK_USER`; anything else → `TASK_USER`. Roles set by the platform sync are stored in `user_roles` and mirrored to Keycloak client roles through the existing `KeycloakAdminService` so tokens carry them.
8. **Platform simulator** (test code only, `src/test/.../PlatformSimulator`): an in-memory MCP client/server that emits the section 4 messages (including duplicates, reordering and failures) so this phase and Phases 3–6 are testable without the real platform.
**How.** MCP tool methods are `@Tool` beans like `ProjectMcpTools`; JSON schemas are generated from the method signatures. Time values parsed with `OffsetDateTime`.
**Acceptance.** Replaying the same stream twice gives identical data; delivering versions 3,1,2 leaves version 3 applied; a node tree sent children-first ends complete; `upsert_user` never inserts into any business table (assert by row counts); a message for an unknown `tenantRef` or from a non-allow-listed client is rejected.
**Tests.** Unit per applier; integration with the simulator for duplicate, out-of-order, parent-missing, cycle, wrong-tenant, wrong-client; row-count assertions for "no business records".
**Rollback.** Tables are additive; disable by `platform.inbound.enabled=false`.

**Built 2026-10-09** (Macro branch `platform-sync/phase-2-inbound-sync`; 603 tests green incl. the opt-in PostgreSQL ones). Deviations from the text above:
- Migrations are **V77–V79** (V76 was taken by the Agile reliability migration) plus control **V2** (`platform_org_id`). Sync copies have **no foreign keys** (messages arrive in any order); `org_node_history.changed_by` became nullable (a platform change has no local user).
- The tenant of a message is found **only** by `platform_tenant.platform_org_id`; a tenant reference or schema name in `tenantRef` is `UNKNOWN_TENANT`. Step 2 of "Turning it on" is therefore a one-line `UPDATE` per tenant until phase 6 provisions it.
- `PENDING_PARENT` is stored as `org_nodes.pending_parent_ref` (the node is a temporary root until its parent arrives); a root node of the platform adopts an existing unlinked root of the same type instead of duplicating it.
- Errors are recorded in `platform_sync_error` in a separate transaction so a rolled-back message still leaves a trace.
- `orgRole` of a membership is accepted but unused in Macro; the organization snapshot is stored in `platform_organization` (open `attributes` JSONB) and not yet mirrored into `app_settings` (phase 3, platform-managed mode).
- **Found by the real-database test:** `User.id` is `@GeneratedValue`, so saving a new `User` silently ignores the id set on it. People are inserted through `UserRepository.insertWithId` so the Keycloak `sub` becomes the key. `UserProvisioningFilter` (existing code) saves new users the old way — to be checked and fixed in phase 5 together with closing its "any realm user" hole.
- `platformUserId` is unique per tenant (`users.platform_ref`); a duplicate is refused as `INVALID_PAYLOAD`.
- `get_state_digest` / `list_aggregate_versions` are still phase 8.

---

### Phase 3 — Macro: outbound (write-through), tool-created users, platform-managed mode  (repo: Macro)
**Goal.** Edits and registrations made inside Macro reach the platform and every other tool, and shared fields can no longer diverge.
**Why.** Test case 1 ("edit anywhere, saved everywhere") and Q6 B.
**What.**
1. `PlatformMcpClient`: obtains a service token (client-credentials, cached until near expiry), calls section 4.3 tools with `idempotencyKey` (UUID stored with the pending action), timeout and bounded retry (`platform.retry`).
2. `WriteThroughService` and a per-tenant flag `platform_managed` (column on `platform_tenant`, default **false**). When true:
   - `UserService.updateUser/changeUserRole/toggleUserStatus`, `OrgNodeService.create/update/move/delete`, and org-profile edits call the platform first; they apply only the snapshot returned.
   - The UI shows shared fields as platform-managed and routes edits through those services (no frontend rewrite beyond labels and error messages).
   - Tool-only fields (department, daily capacity, avatar, project roles) stay local.
3. **Tool-created users (A2).** Change `UserService.createUser` for platform-managed tenants: (a) look the email up in Keycloak (`KeycloakAdminService.findByEmail`, new); reuse `sub` if found; (b) otherwise create with **email = real email**, username = login id; (c) call `report_user_created`; (d) save the local row from the platform's answer; (e) if the platform call fails, **disable the just-created Keycloak user and roll back**, so no orphan identity exists (compensation).
4. Replace Keycloak's `tenant` user attribute usage by the registry (the attribute may remain for old tokens but is not trusted).
5. Failure UX: "Could not reach the platform, nothing was saved. Try again."
**How.** Each write-through call and its result are logged in `platform_sync_error` on failure. The idempotency key makes a retry after a timeout safe.
**Acceptance.** With the simulator: edit a user's name in Macro → simulator receives one `update_user_profile` and Macro shows the returned snapshot; simulator down → nothing changes locally; creating a user whose email exists in Keycloak reuses that `sub`; platform failure after Keycloak creation leaves the Keycloak user disabled.
**Tests.** Unit for compensation; integration with simulator for success, timeout-then-retry (one effect), rejection; regression for non-managed tenants (behaviour unchanged).
**Rollback.** `platform_managed=false` restores old behaviour per tenant.

**Built 2026-10-09** (Macro branch `claude/dazzling-meitner-y26nsv`; 671 tests green on a real PostgreSQL 16, including 11 new whole-application tests; not yet merged). Details: `docs/08-architecture/integrations/INT-eis-platform-sync.md` in the Macro repo. Deviations from the text above:
- **Refused, not written through:** in a managed tenant `changeUserRole`, `toggleUserStatus`, `deleteUser` and a change of email, active status, org-node placement or role in `updateUser` are **refused** ("managed by the platform"). Contract §5 has no tool for membership status, product role or placement, and Q10 puts the product role on the platform. **Open question for the product owner:** add `set_user_access` / `set_membership` / placement tools to contract §5 (then Phase 7 serves them and Macro routes these edits), or keep these changes on the platform's own screens.
- **Organization-profile edits** are not routed: contract §5 has no `update_organization` tool, and the snapshot is not yet mirrored into `app_settings` (phase 2 note).
- **Idempotency key:** one fresh key per user action, reused for the client's automatic repeats (`platform.retry.interactive-attempts`, default 2, new in the connection file). It is **not** stored with a pending action: nobody delivers later, the person is waiting.
- **Contract §5.1 added (additive, version stays 1):** a result carries the new state in `data.snapshots`; the tool applies each entry through the same inbox, version guard and appliers as a delivery. Copied into both repositories. **Phase 7 must return this shape.**
- **Tool-created users (A2):** Keycloak lookup by email (reuse only if its email is verified), real email when creating, `report_user_created`, local row from the answer, Keycloak user disabled if the report fails. The role asked for in the form is not applied (no product access until the platform grants it).
- The Keycloak `tenant` attribute is still written (the legacy token-claim tenant source stays until phase 5).
- No UI change: the screens show the backend's messages; a "this tenant is platform-managed" flag for labelling fields is a small follow-up. The flag is switched by SQL (`platform_managed`).
- Failures are recorded in `platform_sync_error` with `received_from = 'to-platform'`; metric `platformsync.outbound{tool,status}`.
- Known limit: a crash between creating the Keycloak user and reporting it leaves an enabled Keycloak user with no local row; reconcile (phase 8) compares `sub`s.

---

### Phase 4 — Macro: SSO bridge  (repo: Macro)
**Goal.** A person already signed in to EIS opens Macro without a second login; logout propagates both ways. (Macro has no bridge code today.)
**Why.** The EIS bridge lists Macro's backend as a partner (`localhost:8080/api`) but Macro does not implement the partner endpoints, so launch always shows a login page.
**Protocol (from the EIS code, section 2.2).**
- Cookie `vyoog_sso` (HttpOnly, `Secure`, `SameSite=Lax`, shared parent domain via `SSO_COOKIE_DOMAIN`) holds a random id.
- To log a visitor in, the tool backend calls the **other app's** `POST {partner}/internal/sso/token` with body `{ssoSessionId, targetClientId}` and header `X-Internal-Sso-Secret`; the answer is `{accessToken, refreshToken, expiresInSeconds}` for the **tool's own Keycloak client**.
- Logout: `POST {partner}/internal/sso/logout` with `{ssoSessionId}`; always `204`.
**What.**
1. **Consumer side:** `GET /api/auth/session` (new): reads `vyoog_sso`; for each configured partner, calls `/internal/sso/token` (`targetClientId` = `platform.sso.keycloak-client-id`); on success sets Macro's own refresh cookie (HttpOnly) and returns the access token; on none, `401` so the frontend shows the normal login. `POST /api/auth/refresh` and `POST /api/auth/logout` (clears cookies, calls partners' `/internal/sso/logout`).
2. **Provider side:** table `sso_bridge_session` (control schema, one per datasource, not per tenant: it holds refresh tokens, so store them **encrypted** with the existing `SecretCipher`), `POST /api/internal/sso/token` and `/api/internal/sso/logout`, secured by `X-Internal-Sso-Secret` with constant-time comparison, never CORS-exposed. Macro's own password login (`keycloakLogin`) is moved behind a backend endpoint so it can create a bridge session and set `vyoog_sso`.
3. Frontend: on load call `/api/auth/session` first; if `401`, show the login page; keep federated login (`kc_idp_hint`) as is.
4. `/internal/**` permitted without a JWT in `SecurityConfig` **only** with the secret check, excluded from CORS (copy of the EIS rules).
5. Use per-app service clients for sync (Q13); the shared secret remains for the bridge only and is listed in the retirement backlog.
**Acceptance.** Sign in to EIS locally → open Macro → no login prompt; logout in either app ends the other; wrong or missing secret returns 403; a stale cookie falls back to login. Cross-tenant: the token's user must still pass Phase 5 for the tenant of the host.
**Tests.** Unit: cookie handling, secret comparison, encryption round-trip. Integration: a fake partner (WireMock-style) for redeem/refresh/logout. Manual script in `test-cases/` for the two-app browser flow (needs both apps running).
**Rollback.** `platform.sso.enabled=false` restores the old login page only.

**Built 2026-10-09** (Macro branch `claude/dazzling-meitner-y26nsv`, on top of phase 3; 716 tests green on a real PostgreSQL 16; not yet merged). Details: `docs/08-architecture/integrations/INT-eis-platform-sync.md` in the Macro repo. Deviations and findings:
- **Off by default.** `platform.sso.enabled=false`: the new endpoints answer 404 and the SPA falls back to signing in at Keycloak, so nothing changes until it is switched on (the plan's rollback, built as the default).
- **Bridge store** is `platform_control.sso_bridge_session` (control migration **V3**, JDBC like the tenant registry, refresh token AES-GCM encrypted with the existing `SecretCipher`); in memory when `platform.tenants.source=file`.
- **Added beyond the plan:** `platform.sso.allowed-target-clients` — the audiences Macro will mint a token for when EIS redeems a session (the exchange can mint for any person, so the list is explicit, default `eis-platform-ui`); an empty shared secret refuses everyone; `/internal/**` is excluded from CORS and from the host filter; the backend-held client secret means **`VITE_CLIENT_SECRET` is no longer needed in the browser bundle for password sign-in** (it is still used by the old fallback path and by the SAML/OIDC brokering sign-in, which are not part of the bridge).
- **No polling:** a sign-out in EIS reaches Macro's backend at once but a Macro tab learns at its next token renewal (EIS polls every ~20 s). Add polling if that is too slow.
- **Not covered:** the two-app flow against a real Keycloak and a real EIS (`test-cases/integration/platform-sync-sso-manual.md`); the Keycloak impersonation settings are outside both repositories. **Rate limiting of `/auth/login`** is Keycloak's (Macro's `RateLimitFilter` is a stub). The first-login hole (any realm user is created in any tenant) is **still open until phase 5**; the bridge neither widens nor closes it.
- **Security finding in EIS (not changed here):** `backend/src/main/resources/application.yml` has committed defaults for `vyoog.internal.impersonation-client-secret` and `vyoog.internal.sso-shared-secret`. Rotate them and remove the defaults (BR-SEC-001); Macro's side has no committed value.

---

### Phase 5 — Macro: entitlement enforcement and closing the first-login hole  (repo: Macro)
**Goal.** Every protected Macro request is checked against organization, subscription, product access and provisioning, and a valid realm token can no longer create itself in any tenant.
**Why.** Today any realm user who reaches a tenant host is auto-provisioned there; authentication alone grants access. This is the largest security gap found.
**What.**
1. `EntitlementService` returns `{allowed, reason}` from local tables (`platform_organization.status`, `platform_subscription` with `starts_at <= now <= ends_at`, `platform_user_access` active, tenant status READY, local user active), cached for `entitlement.cache-ttl-seconds` and invalidated when an inbound message changes the relevant rows.
2. `EntitlementFilter` (after authentication, before controllers): resolves the tenant (host proposes, registry confirms, user's `sub` must exist in that tenant), runs the check, denies with `403` and a machine-readable `reason` (`NO_SUBSCRIPTION`, `SUBSCRIPTION_ENDED`, `NO_PRODUCT_ACCESS`, `ORG_INACTIVE`, `NOT_PROVISIONED`, `USER_INACTIVE`).
3. Sensitive actions (a list in `EntitlementPolicy`: user and role administration, org-structure changes, data export, approvals/baseline changes, settings) call `get_entitlement` centrally; `fail-closed: true` means a platform outage denies them.
4. `UserProvisioningFilter`: in platform-managed tenants **do not** create or adopt rows; return `403 NOT_A_MEMBER`. (Rows arrive only through `upsert_user` or `report_user_created`.) Keep the old behaviour for non-managed tenants.
5. Matching rule (Q5) implemented in `UserApplier`; `UserIdentityReconciliationService` is called only from there.
6. Frontend: show an "access denied / subscription ended" page keyed by `reason`; hide nothing as a security measure.
**Acceptance (matrix).** Active subscription + access + provisioned → allowed. No subscription → denied. After `ends_at` (23:59:00.000 +05:30) → denied at the next request after cache refresh and at once for sensitive actions. No product access → denied. Suspended membership → denied. Unprovisioned tenant → denied. A realm user from another organization → `NOT_A_MEMBER`. Wrong-audience token → rejected. Spoofed `Origin` → no effect on the resolved tenant (user must belong to it). Another tool's access does not grant Macro access.
**Tests.** Table-driven unit test of every row above; filter integration test with MockMvc and the simulator; regression of existing endpoints with an "entitled" fixture user.
**Rollback.** `platform.entitlement.enforce=false` for non-production environments only; production keeps it on.

---

### Phase 6 — Macro: tenant provisioning and adoption of existing tenants  (repo: Macro)
**Goal.** A new organization gets a ready tenant automatically; today's tenants are attached to platform organizations safely.
**Why.** Needed when the platform sells the first subscription to a new customer. Hosting details (Q9) are decided later, so this phase isolates them behind `datasource_ref`.
**What.**
1. `provision_tenant` tool → `TenantProvisioner`: validate `tenantRef` and `datasource_ref` (the ref must already exist in server-side configuration; a request can never carry a URL, database or schema name), create the schema if missing, run `ToolAdapter.migrateSchema` (Flyway with the schema as `defaultSchema`), create the organization root node and the first admin from the snapshot, write `schema_version`, set status READY, return it. Safe to call twice.
2. On failure: status FAILED with `last_error`; the call returns `retry`; a later identical call resumes.
3. **Adoption** of `vyoog_pms` and `vyoog_pms_vyoog`: a one-off admin command (`PlatformAdoptionRunner`, dry-run first) that sets `platform_ref` on the root node and users by matching Keycloak `sub`, reports unmatched rows, and makes **no destructive change**. Users without a platform counterpart are listed, not deleted.
4. **Documentation of the later move** to "database per client": the registry already supports it (new row = new `datasource_ref`); moving an existing tenant is `pg_dump` of the schema, restore into the client database, flip the registry row, with backup and rollback steps written in `deployment/`. Not executed in this phase.
**Acceptance.** Provisioning twice leaves one schema and one root; a failed migration is visible and retryable; adoption dry run lists matches/unmatched and changes nothing; tenant A cannot read tenant B.
**Tests.** Integration on local Postgres with two databases; negative tests for client-supplied schema names.
**Rollback.** Registry row status SUSPENDED; adoption is additive columns only.

---

### Phase 7 — Platform side  (repo: EIS)
**Goal.** The platform produces the contract: events, versions, delivery, MCP server, registry, monitor.
**Why.** Until now Macro was tested against a simulator.
**What.**
1. **Expiry (A3).** One `SubscriptionClock` helper: `endOfDay(date)` = 23:59:00.000 Asia/Kolkata. Use it in `firstRenewalDate`, renewal, cancel-at-period-end and the expiry job. Migration **V028** normalizes existing `expires_at` to that time on the same IST date. Add a test across a DST-free zone, month-ends and leap days. Update invoice/renewal tests that assume UTC midnight.
2. **Versions.** Add `version BIGINT` (incremented on every change) to `organization`, `org_node`, `customer`, `organization_member`, `product_subscription`, `organization_product_access`. Increment in the service layer inside the same transaction.
3. **New event types** in `PlatformEventTypes` (names follow the existing style): `OrganizationUpserted`, `OrgNodeUpserted`, `OrgNodeDeleted`, `UserUpserted`, `MembershipChanged`, `UserProductAccessGranted`, `UserProductAccessRevoked`, `TenantProvisioningRequested`, plus the existing subscription events now carry `endsAt` per A3.
4. **Registry tables** (migration **V029** + `schema.sql`): `tool_connector(product_id, base_mcp_url, client_id, contract_version, status)`; `tenant_app_schema(organization_id, product_id, tenant_ref, datasource_ref, schema_name, status, schema_version, last_synced_version, last_error)`. A client database registry (`client_database`) is added only when Q9 is decided.
5. **Delivery.** A new `PlatformEventHandler` implementation `ToolDeliveryHandler`: for each event, finds the tools the organization has an **active, provisioned** subscription for (never others), builds the contract message, calls the tool's MCP tool via `PlatformMcpClient`; the existing `event_handler_receipt` is the per-destination status (handler id = `tool:<productCode>`); retry/backoff is the existing dispatcher's, extended to the section 5.1 retry settings.
6. **Platform MCP server** at `/api/mcp` (hand-wired, `spring-ai-mcp`, as Macro did, Boot 3.2.5 compatible) with the section 4.3 tools; guard = `azp` allow-list + active-subscription check for the `tenantRef`; idempotency table `mcp_idempotency(key, tool, result_json, created_at)`.
7. **Publishers** in the existing services: hierarchy (`OrgHierarchyService`), members, invitations (acceptance emits `UserUpserted` + `MembershipChanged`), product access, subscriptions.
8. **Keycloak.** Create service clients `eis-sync`, `thittam-sync` (and one per future tool) with only the roles they need; document in `deployment/`.
9. **Admin monitor.** Extend `/admin/integrations/events` with a "Tool sync" tab: per organization × product status, last success, last error, attempts, version lag, Retry/Replay (permission `MANAGE_INTEGRATIONS`, audited).
10. **Reconcile job** (Phase 8 uses it): `ReconcileService` calls `get_state_digest`, compares, and replays missing aggregates.
**Acceptance.** An organization change appears in the Macro simulator **and** a real Macro within seconds; a stopped tool does not delay other tools; retries then `FAILED` are visible; `Replay` redelivers; expiry appears as `…T23:59:00.000+05:30`.
**Tests.** Unit and integration in the existing style (`mvn -B -o verify`, `EIS_PG_TEST_PASSWORD=root`); `LayeredArchitectureTest` kept green; frontend tests for the new tab; test cases `TC-INT-0xx`.
**Rollback.** `app.events.tool-delivery.enabled=false`; migrations additive except the expiry normalization (take a backup first; script a reverse that is exact only for rows touched).

---

### Phase 8 — Connect, reconcile, harden  (both repos)
**Goal.** Prove the two real systems work together, and recover from loss.
**What.**
1. End-to-end scenarios on a staging pair (EIS + Macro + Keycloak), each recorded as a `test-cases/` document with expected rows: create organization → provision; add node → appears; move node → same structure; invite and accept → user in Macro with mapped role and node; edit user in Macro → appears in EIS and another tool simulator; subscription ends → Macro denies; grant/revoke product access; tool down then up.
2. Reconcile in production mode: schedule `platform.reconcile.cron`; metrics `platformsync.drift.detected`, `platformsync.delivery.failed`, `platformsync.entitlement.denied{reason}`, `platformsync.lag.seconds`; alert thresholds documented.
3. Security pass: secrets scan, `azp` allow-list review, rate limit on the MCP endpoints, log review for personal data, penetration-style tests for tenant spoofing.
4. Retirement plan for `INTERNAL_SSO_SHARED_SECRET` (Q13): replace with per-app clients for the bridge's backend calls; separate change, scheduled later.
**Acceptance.** All scenarios pass; a deliberately deleted projection row is repaired by reconcile; a killed tool recovers by itself; metrics visible.

---

### Phase 9 — Roll out to the next tools  (each tool repo)
**Goal.** Add Valam and others by copying the module, not redesigning it.
**What.** For each tool: (1) copy `platformsync` and `platform-connection.yml`; (2) implement `ToolAdapter` (role map, node/user tables, migrations); (3) add the tool's service client in Keycloak and a `tool_connector` row in the platform; (4) run the Phase 2–6 acceptance tests with the simulator, then Phase 8 scenarios; (5) adopt existing tenants. The contract version is bumped only for breaking changes; a tool announces the versions it supports.
**Checklist** is `docs/09-integrations/tool-onboarding-checklist.md` (written in Phase 8).

---

## 7. Cross-cutting rules for every phase
1. Read `CLAUDE.md` of the repo first; follow its layering, naming, test and documentation rules.
2. One migration file per change, additive, never edited after it is applied; qualify with the placeholder schema; EIS also mirrors changes in `schema.sql`.
3. Never put a secret, token, schema name or database name in code or in a request; all come from server-side configuration.
4. Never create business records from profile sync.
5. Every inbound apply is one transaction: inbox row, version, data.
6. Do not report success to a caller before the data is committed.
7. Every phase ends with: tests run and listed, docs updated, a short "what changed and why" for each file.
8. Report failures honestly; do not skip or weaken a test to pass.

## 8. Risks
| Risk | Mitigation |
|---|---|
| Changing the Hibernate connection provider breaks existing tenants | Phase 1 keeps both tenants on the same datasource and schemas; fallback property for one release. |
| Single writer makes the platform a dependency for edits | Accepted (A6); entitlement reads are local, only writes and sensitive checks need the platform. |
| Sync lag leaves access open after expiry | Expiry is a stored timestamp, so the tool denies after `ends_at` even if no event arrives; cancellation/revocation use the 5-minute TTL and central check for sensitive actions. |
| Expiry normalization changes billing dates | Backup, dry-run report of changed rows, review before applying (Phase 7). |
| Many tenant pools exhaust connections | Lazy, small pools; hosting decision (Q9) before the first client database. |
| Tool-created Keycloak users (Q6 B) drift from the platform | A2 rules, compensation on failure, reconcile compares `sub`s. |

## 9. Open questions that still need you
1. Confirm or change assumptions **A1–A6** (section 1).
2. Confirm the Thittam role map: `PMS_ADMIN→ADMIN`, `PMS_MANAGER→PROJECT_OWNER`, `PMS_USER→TASK_USER`. Where are the product roles `PMS_*` defined today in EIS (the catalog)? Phase 0 checks, and may add Macro's roles to the catalog.
3. Q9 hosting (before the first client database, not before Phase 7).
4. Name of the Valam repository (needed for Phase 9).
5. Preferred requirement ids and sprint for the new FRD (proposal: `REQ-INT-003`, sprint to be set by you).
