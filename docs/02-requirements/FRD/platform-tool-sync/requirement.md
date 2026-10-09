# REQ-INT-003 — Platform ↔ tool synchronization

**Status:** In Review (written 2026-10-09; waiting for the product owner's "Approved")
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C86](../../../01-business/roadmap/open-decisions.md#c86) (answers Q1–Q14 of 2026-10-09)
**Built:** Not built. Build order and technical design: [phase plan](../../../09-integrations/platform-tool-sync-plan.md). Wire contract: [contract v1](../../../09-integrations/platform-tool-contract-v1.md). Test plan: [TESTPLAN-INT-003](../../../../test-cases/functional/platform-tool-sync/TESTPLAN-INT-003.md).

| Field | Value |
|---|---|
| Sprint | Not scheduled — the product owner sets it. Macro-side work (phases 1–6) is planned before the platform side (phase 7) |
| Requirement ID | REQ-INT-003 |
| Application | [13 Integration & API Platform](../../../01-business/roadmap/applications/13-integration-api-platform.md), with [09 Order & Provisioning](../../../01-business/roadmap/applications/09-order-provisioning-management.md) and [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-INT` |
| Priority | P0 |
| AI required | No |

## Source functions
| Function | Covered here |
|---|---|
| 09.02.01.01–.05 Provision, configure, activate, suspend, deprovision service | Messages and data only; the provisioning *contract* stays in [REQ-ORD-002](../provisioning-contract/requirement.md), which this FRD answers (see its "Answers applied") |
| 05.01 Organization & hierarchy, 05.03 Users & membership, 07.* Subscriptions & access | Their data is **published** to tools. Their business rules stay in their own FRDs |

## Summary
The platform is the hub for organizations, hierarchy, users, memberships, subscriptions and product access. Each product (a **tool**) keeps its own database schema and a **copy** of the data it needs. When something changes on the platform, every tool the organization has subscribed to receives it reliably. When a person registers or edits a shared field inside a tool, the change goes through the platform first, and then reaches every tool. Tools refuse access when the organization's subscription or the person's product access is not valid.

## Actors
- **Platform administrator** — monitors synchronization, retries and replays.
- **Organization administrator** — changes organization data, hierarchy, members, product access on the platform; sees the same data in every tool.
- **Member / user** — registers or edits their profile in the platform or in a tool.
- **Tool (service client)** — a product backend acting with its own Keycloak service client.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-INT-003.1 | **One contract for all tools.** Messages and tools follow [contract v1](../../../09-integrations/platform-tool-contract-v1.md): an envelope with `contractVersion`, `eventId`, `eventType`, `occurredAt`, `aggregateType`, `aggregateId`, `version`, `tenantRef`, `payload`. A tool declares the contract versions it supports; a breaking change raises the version. | Must |
| REQ-INT-003.2 | **Published data (aggregates):** organization, org node (with levels), user, membership (status and node placement), subscription, user product access (with the product role). Nothing else is published without a change to this FRD. | Must |
| REQ-INT-003.3 | **Only to subscribed, provisioned tools.** An event is delivered to a tool only if the organization has an ACTIVE or SUSPENDED subscription for that tool **and** the tool's tenant for the organization is provisioned (READY). Never to other tools. | Must |
| REQ-INT-003.4 | **Versions.** Every published aggregate has a version that increases on every change, inside the same transaction as the change. A receiver ignores any message whose version is not greater than the one it holds, so late or repeated messages cannot undo newer data or restore revoked access. | Must |
| REQ-INT-003.5 | **Reliable delivery.** Events use the existing outbox ([REQ-INT-002](../event-platform/requirement.md)): written in the same transaction as the change, delivered by the dispatcher, **at least once**, in order per aggregate. Delivery to each tool is its own handler with its own receipt, so one unavailable tool never blocks another. | Must |
| REQ-INT-003.6 | **Retry and failure.** Temporary failures are retried with exponential backoff up to a configured limit; then the delivery is FAILED, shown to administrators, and can be retried or replayed. A delivery is "delivered" only when the tool answered `applied` or `duplicate`. | Must |
| REQ-INT-003.7 | **Platform MCP server.** The platform exposes the contract's tool-to-platform tools (register/report user, update profile, hierarchy changes, entitlement check, provisioning report) over MCP at `/api/mcp`. MCP is the **transport** (decision Q1); reliability comes from this FRD, not from MCP. | Must |
| REQ-INT-003.8 | **Single writer.** Centrally owned fields are changed only by the platform. A change made inside a tool is sent to the platform with an idempotency key; the platform validates and commits it, publishes it to all tools and returns the snapshot, which the tool applies. If the platform cannot be reached, the tool refuses the edit and writes nothing locally. | Must |
| REQ-INT-003.9 | **Identity key.** A person is identified across products by the **Keycloak user id (`sub`)**. The platform never merges two people because an unverified email matches. A pre-existing local row in a tool may be linked by email only when the Keycloak email is verified, and every such link is recorded. | Must |
| REQ-INT-003.10 | **Tool-created users.** A tool may create a Keycloak user directly (decision Q6) only if it first looks the person up in Keycloak by email and reuses the existing user, creates new users with the **real email**, and then reports the user to the platform (`report_user_created`) with an idempotency key. If the report fails, the tool disables the user it just created. From then on the platform's record is authoritative. | Must |
| REQ-INT-003.11 | **No business records from profile sync.** Receiving or applying a user, membership or access message never creates employees, projects, tasks, payroll, sales or any other product record. Product records are created only by that product's own workflows. | Must |
| REQ-INT-003.12 | **What is copied.** A tool receives the organization record **with every field the platform holds** (decision Q3), as defined columns plus a free-form `attributes` object, so fields added later reach tools without a tool migration; user and membership fields as listed in the contract. **Never** published: passwords, tokens, MFA secrets, payment or bank data, invoices, internal notes. | Must |
| REQ-INT-003.13 | **Subscription end time.** A subscription's end is `YYYY-MM-DD 23:59:00.000 +05:30` (Asia/Kolkata), published as an ISO-8601 time with that offset. Access is allowed while `startsAt ≤ now ≤ endsAt` and denied immediately afterwards (no trial, no grace period for now). See [BR-SUB-010](../../../03-business-rules/BR-SUB-010-subscription-end-time.md). | Must |
| REQ-INT-003.14 | **Product access and role.** The platform publishes each member's product access with the **product role** (existing `productRole`). The tool maps it to its own roles and may refine within the tool. Access exists only for products in the organization's ACTIVE subscriptions ([REQ-TEN-005](../access-management/requirement.md)). | Must |
| REQ-INT-003.15 | **Entitlement checks.** A tool checks every protected request against its local copy (organization active, subscription effective, product access active, tenant provisioned, user active) and, for **sensitive actions**, asks the platform (`get_entitlement`). The local copy must not stay valid longer than 5 minutes after a revocation; if the platform cannot answer a sensitive check, the action is denied (fail closed). | Must |
| REQ-INT-003.16 | **Tenant link.** The platform keeps, per organization and tool, a registry entry: tenant reference, datasource reference, schema name, status (PENDING, READY, FAILED, SUSPENDED), schema version, last synced version, last error. The platform never sends or accepts a database or schema name inside a message; destinations are resolved from the registry. | Must |
| REQ-INT-003.17 | **Provisioning requests.** When an organization's subscription to a tool starts, the platform asks the tool to provision the tenant (`provision_tenant`, idempotent). The tool reports the result. Provisioning never deletes data; expiry and cancellation never delete a schema (retention, export and deletion are separate workflows, not in this FRD). | Must |
| REQ-INT-003.18 | **Reconcile.** A periodic job compares each tool's state digest with the platform's and resends what is missing or stale. | Must |
| REQ-INT-003.19 | **Security.** Each tool has its own Keycloak service client; the platform too. A receiver validates the token (signature, issuer, expiry), checks the caller's `azp` against an allow-list, and for tool-to-platform calls confirms the tool has an active subscription for the `tenantRef` in the call. Secrets come from environment or a secrets manager; none is committed. | Must |
| REQ-INT-003.20 | **Audit.** The platform records provisioning requests, retries and replays, tool-reported users, write-through changes (with the tool as the source) and entitlement checks that deny. | Must |
| REQ-INT-003.21 | **Admin monitor.** A platform administrator sees, per organization and tool: provisioning status and schema version, last successful sync, last error, attempts, version lag, and can retry or replay (permission `MANAGE_INTEGRATIONS`, audited). | Must |
| REQ-INT-003.22 | **Same structure in every tool.** Each tool uses the standard `platformsync` module, one connection file `platform-connection.yml`, and one adapter (see the [phase plan](../../../09-integrations/platform-tool-sync-plan.md#5-the-standard-tool-module-same-structure-in-every-tool)). | Should |
| REQ-INT-003.23 | **Browser launch.** Tools take part in the existing EIS single-sign-on bridge (cookie `vyoog_sso`, `/internal/sso/token`, `/internal/sso/logout`), so a person signed in to EIS opens a tool without a second login. Authentication never implies access: REQ-INT-003.15 still applies. | Should |

## Answers applied on 2026-10-09 ([C86](../../../01-business/roadmap/open-decisions.md#c86))
| Question | Answer |
|---|---|
| Q1 | MCP is the transport; reliability from the platform (outbox, retries, idempotency keys, reconcile). |
| Q2 | Full hierarchy sync (nodes, levels, moves, deletes) and each user's node placement. |
| Q3 | Tools store everything the platform has about an organization (open snapshot, see .12). |
| Q4 | Keycloak user id is the cross-product user key. |
| Q5 | Match by Keycloak id; email link only when verified, always logged. |
| Q6 | Tools may create Keycloak users and report them (with the guard rails of .10). |
| Q7 | Runtime tenant registry and routing datasource; database per client, schema per app; the host does not decide the tenant. |
| Q8 | Individuals are one-person personal organizations on the same tenant machinery. |
| Q9 | Hosting and scale decided later; registry holds a datasource *reference*. |
| Q10 | The platform stores the product role; the tool maps and may refine. |
| Q11 | REQ-TEN-005 approved as drafted; expiry means immediate denial; end time 23:59:00.000 +05:30. |
| Q12 | Hybrid entitlements; revoke within about 5 minutes. |
| Q13 | Keep the SSO bridge; one service client per app for sync; retire the shared secret later. |
| Q14 | FRDs first, approved, then build per repo. |

## Defaults applied until the product owner confirms (assumptions A1–A6 of the plan)
Open snapshot with `attributes` for organization fields (A1); the tool-created-user guard rails (A2); end time stored as an instant equal to 23:59:00.000 +05:30 (A3); Macro first, platform second (A4); "connection metrics in one file" means one connection-settings file plus one metrics class (A5); single writer (A6).

## Out of scope
Per-tool bulk historical import beyond the one-time adoption of existing tenants (a phase-6 task of the plan), trial and grace periods, billing data in tools, translating tool UI, retention/export/deletion workflows for tenant data, choosing database hosting (Q9), replacing the shared SSO secret (backlog), tools other than the first (they reuse the contract in their own sprints).

## Not specified (not invented)
- Hosting of client databases and connection pooling limits (Q9).
- Retention period of a tenant's schema after a subscription ends.
- Whether a person may belong to several organizations (still one active organization per person, [REQ-TEN-008](../invite-user/requirement.md)).
- Notification of people when their product access changes (REQ-TEN-005 Open question 6).

## Dependencies
[REQ-INT-002](../event-platform/requirement.md) (outbox), [REQ-ORD-002](../provisioning-contract/requirement.md) (provisioning), [REQ-TEN-005](../access-management/requirement.md) (access and product roles), [REQ-TEN-006](../org-hierarchy/requirement.md) (hierarchy), [REQ-TEN-008](../invite-user/requirement.md) (membership), [REQ-SUB-001](../subscription-lifecycle/requirement.md), [REQ-SUB-003](../subscription-seats/requirement.md), [REQ-SUB-004](../renewal-reminders/requirement.md) (end time), Keycloak, [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md) (secrets).
