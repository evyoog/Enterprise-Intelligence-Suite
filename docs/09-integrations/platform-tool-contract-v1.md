# Platform ↔ tool contract — version 1

**Status:** Approved 2026-10-09 (with [REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md)). Not implemented.
**Audience:** engineers and agents implementing the platform side or a tool side. Copy this file unchanged into each tool repository (`docs/…/platform-tool-contract-v1.md`); a change here is a contract change.
**Transport:** MCP (JSON-RPC 2.0 over Streamable HTTP, protocol 2025-06-18) at `/api/mcp` on both sides. MCP carries the calls; delivery guarantees come from the platform (outbox, retries, idempotency, reconcile) — see the [phase plan](platform-tool-sync-plan.md).

---

## 1. Conventions

| Topic | Rule |
|---|---|
| Identifiers | JSON strings. Platform ids are strings of the platform's numeric ids (`"4812"`). People are identified by `sub`, the Keycloak user id (UUID string). |
| Time | ISO-8601 with an offset, millisecond precision. Subscription `endsAt` is always `…T23:59:00.000+05:30` ([BR-SUB-010](../03-business-rules/BR-SUB-010-subscription-end-time.md)). `occurredAt` uses the platform's local offset `+05:30`. |
| Versions | `version` is an integer that increases by at least 1 on every change of that aggregate, per aggregate (not global). |
| Nulls | A field the platform does not have is omitted, not `null`, unless the schema says otherwise. Unknown fields must be ignored by receivers (forward compatibility). |
| Strings | UTF-8, trimmed by the sender. Email is lower-cased by the sender. |
| Never sent | Passwords, tokens, MFA secrets, payment or bank data, invoices, internal notes. |
| Contract version | `contractVersion: "1"`. A receiver that does not support the version answers `rejected` with reason `UNSUPPORTED_CONTRACT_VERSION`. Adding optional fields does not change the version; removing or changing the meaning of a field does. |

## 2. Envelope

### JSON Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "envelope.v1",
  "type": "object",
  "required": ["contractVersion", "eventId", "eventType", "occurredAt", "aggregateType", "aggregateId", "version", "tenantRef", "payload"],
  "properties": {
    "contractVersion": { "const": "1" },
    "eventId":        { "type": "string", "format": "uuid" },
    "eventType":      { "type": "string", "maxLength": 80 },
    "occurredAt":     { "type": "string", "format": "date-time" },
    "aggregateType":  { "enum": ["Organization", "OrgNode", "User", "Membership", "Subscription", "UserAccess"] },
    "aggregateId":    { "type": "string", "maxLength": 100 },
    "version":        { "type": "integer", "minimum": 1 },
    "tenantRef":      { "type": "string", "maxLength": 100 },
    "payload":        { "type": "object" }
  },
  "additionalProperties": false
}
```

### Example
```json
{
  "contractVersion": "1",
  "eventId": "7c1f3a52-9d1e-4a0a-8d0e-2b9d5c3b0a11",
  "eventType": "OrgNodeUpserted",
  "occurredAt": "2026-10-09T10:15:30.000+05:30",
  "aggregateType": "OrgNode",
  "aggregateId": "4812",
  "version": 17,
  "tenantRef": "100",
  "payload": { "id": "4812", "parentId": "4800", "name": "Press Shop", "type": "DEPARTMENT",
               "code": "PRS", "description": "Presses", "sortOrder": 2, "active": true }
}
```

`tenantRef` is the platform organization id. The receiver resolves it through its own tenant registry. **It never accepts a database name, schema name, host or connection detail from a message.**

## 3. Snapshots (payloads)

### 3.1 Organization (`aggregateType: Organization`)
All organization fields the platform holds are sent (decision Q3). Stable fields are named; everything else goes in `attributes` so that new fields reach tools without a tool migration.

```json
{
  "$id": "organization.v1",
  "type": "object",
  "required": ["id", "name", "status"],
  "properties": {
    "id":              { "type": "string" },
    "code":            { "type": "string" },
    "name":            { "type": "string" },
    "type":            { "type": "string" },
    "status":          { "enum": ["ACTIVE", "SUSPENDED", "CLOSED"], "description": "the platform's lifecycle status" },
    "parentOrganizationId": { "type": "string" },
    "licensedSeats":   { "type": "integer" },
    "attributes":      { "type": "object", "description": "every other organization field, e.g. industry, website, businessEmail, phone, country, state, city, address, billing address, gstin, pan, companyRegistrationNumber, taxVatNumber, regionId, mfaRequired" }
  }
}
```
Note: tax registration numbers and addresses are organization profile data and are included under "everything the platform has". Bank, card and payment data are **not** organization fields and are never sent.

### 3.2 OrgNode (`aggregateType: OrgNode`) — event types `OrgNodeUpserted`, `OrgNodeDeleted`
```json
{
  "$id": "org-node.v1",
  "type": "object",
  "required": ["id", "name", "type", "active"],
  "properties": {
    "id":          { "type": "string" },
    "parentId":    { "type": "string", "description": "omitted for the root" },
    "name":        { "type": "string", "maxLength": 150 },
    "type":        { "type": "string", "maxLength": 50, "description": "level type, e.g. ORGANIZATION, DIVISION, BUSINESS_UNIT, DEPARTMENT, LOCATION, COST_CENTER, TEAM" },
    "levelRank":   { "type": "integer", "description": "rank of the type in the level order (0 = top)" },
    "code":        { "type": "string", "maxLength": 50 },
    "description": { "type": "string" },
    "sortOrder":   { "type": "integer" },
    "active":      { "type": "boolean" }
  }
}
```
`OrgNodeDeleted` carries `{ "id": "4812" }`. Level types and ranks that a receiver does not know are added to its level configuration, never rejected.

### 3.3 User (`aggregateType: User`) — event type `UserUpserted`
```json
{
  "$id": "user.v1",
  "type": "object",
  "required": ["sub", "email", "firstName", "lastName", "status"],
  "properties": {
    "sub":        { "type": "string", "format": "uuid", "description": "Keycloak user id; the cross-product key" },
    "platformUserId": { "type": "string", "description": "the platform's customer id" },
    "email":      { "type": "string", "format": "email" },
    "emailVerified": { "type": "boolean" },
    "firstName":  { "type": "string" },
    "lastName":   { "type": "string" },
    "phone":      { "type": "string" },
    "status":     { "enum": ["ACTIVE", "INACTIVE"] },
    "attributes": { "type": "object", "description": "other profile fields the platform holds, excluding credentials" }
  }
}
```
The aggregate id of a User is the `sub`.

### 3.4 Membership (`aggregateType: Membership`) — event types `MembershipChanged`
```json
{
  "$id": "membership.v1",
  "type": "object",
  "required": ["sub", "status", "orgRole"],
  "properties": {
    "sub":      { "type": "string", "format": "uuid" },
    "status":   { "enum": ["ACTIVE", "SUSPENDED", "INACTIVE"] },
    "orgRole":  { "enum": ["ORG_ADMIN", "MEMBER"] },
    "nodeId":   { "type": "string", "description": "the person's node placement; omitted when unplaced" }
  }
}
```
Aggregate id: `sub`. A tool deactivates the local user when `status` is not `ACTIVE`.

### 3.5 Subscription (`aggregateType: Subscription`) — event types `SubscriptionActivated`, `SubscriptionChanged`, `SubscriptionSuspended`, `SubscriptionResumed`, `SubscriptionCancelled`, `SubscriptionExpired`, `SeatsChanged`
```json
{
  "$id": "subscription.v1",
  "type": "object",
  "required": ["productCode", "status", "startsAt", "endsAt"],
  "properties": {
    "productCode": { "type": "string", "description": "the product's code in the platform catalog" },
    "plan":        { "type": "string" },
    "status":      { "enum": ["PENDING_SUBSCRIPTION", "ACTIVE", "SUSPENDED", "CANCELLED", "EXPIRED"] },
    "startsAt":    { "type": "string", "format": "date-time" },
    "endsAt":      { "type": "string", "format": "date-time", "pattern": "T23:59:00\\.000\\+05:30$" },
    "seats":       { "type": "integer", "minimum": 1 },
    "autoRenew":   { "type": "boolean" }
  }
}
```
Aggregate id: the platform subscription id. A tool allows use only while `status = ACTIVE` and `startsAt ≤ now ≤ endsAt`.

### 3.6 UserAccess (`aggregateType: UserAccess`) — event types `UserProductAccessGranted`, `UserProductAccessRevoked`
```json
{
  "$id": "user-access.v1",
  "type": "object",
  "required": ["sub", "productCode", "productRole", "status"],
  "properties": {
    "sub":         { "type": "string", "format": "uuid" },
    "productCode": { "type": "string" },
    "productRole": { "type": "string", "description": "the platform's product role, e.g. PMS_ADMIN, PMS_MANAGER, PMS_USER" },
    "status":      { "enum": ["ACTIVE", "INACTIVE"] }
  }
}
```
Aggregate id: `<sub>:<productCode>`. The tool maps `productRole` to its own roles through its adapter; an unknown role maps to the tool's least-privileged role.

## 4. Tools the platform calls on a tool (served by the tool)

Every call: the MCP `arguments` are an object; every result is the **result object** of section 6.

| Tool | Arguments | Behaviour |
|---|---|---|
| `provision_tenant` | `tenantRef`, `datasourceRef`, `organization` (3.1), `firstAdmin` (3.3 + 3.4), `schemaVersion` | Resolve `datasourceRef` from **server-side** configuration (unknown → `rejected` `UNKNOWN_DATASOURCE_REF`); create the schema if missing; run the tool's migrations; create the organization root and first admin; set the tenant READY; return `schemaVersion`. Repeating the call changes nothing. |
| `upsert_organization` | envelope (Organization) | Insert or update the organization copy; keep the root node name in step. |
| `upsert_org_node` | envelope (OrgNode) | Insert or update by platform id. Parent unknown → stored as pending and placed when the parent arrives. A move that creates a cycle → `rejected` `CYCLE`. |
| `delete_org_node` | envelope (OrgNode) | Delete if it has no children and no placed users; otherwise `rejected` `NODE_IN_USE` (reconcile reports it). |
| `upsert_user` | envelope (User) | Upsert by `sub`. Link an existing unlinked row by email only if `emailVerified` is true (record the link). **Never creates product business records.** |
| `set_membership` | envelope (Membership) | Set status, role and node; deactivate the local user when not ACTIVE. |
| `set_subscription` | envelope (Subscription) | Upsert the subscription copy; clear the entitlement cache. |
| `set_user_access` | envelope (UserAccess) | Upsert access; map the role; clear the cache. |
| `get_state_digest` | `tenantRef`, `aggregateTypes[]` | Per type: `{count, hash}` where hash = SHA-256 over the sorted list of `id:version` lines joined by `\n`. |
| `list_aggregate_versions` | `tenantRef`, `aggregateType`, `afterId?`, `limit?` (≤ 1000) | Pairs `{id, version}` ordered by id, for reconcile. |

## 5. Tools a tool calls on the platform (served by the platform)

Every call carries `idempotencyKey` (UUID chosen by the caller, stored by the platform with the result for at least 7 days) and `tenantRef`. The platform answers a repeat with the **first** result.

| Tool | Arguments | Behaviour |
|---|---|---|
| `report_user_created` | `idempotencyKey`, `tenantRef`, `sub`, `email`, `emailVerified`, `firstName`, `lastName`, `phone?`, `nodeId?` | Link or create the customer and membership by `sub` (never by an unverified email). Returns the User and Membership snapshots with versions. The person gets **no** product access. |
| `update_user_profile` | `idempotencyKey`, `tenantRef`, `sub`, `changes` (any of firstName, lastName, phone, attributes), `expectedVersion?` | Validate, commit, bump the version, fan out. `expectedVersion` lower than current → `rejected` `STALE_VERSION` with the current snapshot. |
| `create_org_node` / `update_org_node` / `move_org_node` / `delete_org_node` | `idempotencyKey`, `tenantRef`, node fields or `{id, newParentId}` / `{id}`, `expectedVersion?` | Same rules as the platform's hierarchy service (level order, unique sibling names, no cycles, no delete while in use). |
| `get_entitlement` | `tenantRef`, `sub`, `productCode`, `action?` | Authoritative check: `{allowed, reason, endsAt, productRole}`. Reasons: `OK`, `NOT_A_MEMBER`, `USER_INACTIVE`, `ORG_INACTIVE`, `NO_SUBSCRIPTION`, `SUBSCRIPTION_ENDED`, `NO_PRODUCT_ACCESS`, `NOT_PROVISIONED`. |
| `report_provisioning_result` | `tenantRef`, `productCode`, `status` (`READY`/`FAILED`), `schemaVersion?`, `error?` | Update the tenant registry entry. |

## 6. Result object (every tool)

```json
{
  "$id": "result.v1",
  "type": "object",
  "required": ["status"],
  "properties": {
    "status":  { "enum": ["applied", "duplicate", "rejected", "retry"] },
    "version": { "type": "integer", "description": "the version now held for the aggregate" },
    "reason":  { "type": "string", "description": "machine-readable code, see below" },
    "message": { "type": "string", "description": "short human text, no personal data" },
    "data":    { "type": "object", "description": "snapshots or digest, depending on the tool" }
  }
}
```
| `status` | Meaning | Caller does |
|---|---|---|
| `applied` | Done. | Records success. |
| `duplicate` | Already applied (same `eventId`, or version not newer). | Records success. |
| `retry` | Temporary problem (database busy, dependency unavailable). | Retries with backoff. |
| `rejected` | Permanent (invalid, not allowed, unknown). | Stops and records the reason. |

Reason codes: `UNSUPPORTED_CONTRACT_VERSION`, `UNKNOWN_TENANT`, `TENANT_NOT_READY`, `UNKNOWN_DATASOURCE_REF`, `NOT_ALLOWED_CLIENT`, `NO_ACTIVE_SUBSCRIPTION_FOR_TENANT`, `INVALID_PAYLOAD`, `CYCLE`, `NODE_IN_USE`, `STALE_VERSION`, `EMAIL_NOT_VERIFIED`, `INTERNAL`. An unexpected exception is `retry` with `INTERNAL` until the retry limit.

Retry policy (set in each tool's connection file, defaults): up to 8 attempts, exponential backoff from 5 s to 15 min.

## 7. Authentication and authorization

1. Caller obtains a token from Keycloak with **client credentials** using its own service client (`eis-sync` for the platform, `<tool>-sync` for a tool).
2. The receiver validates signature, issuer, expiry.
3. The receiver checks the token's `azp` against its allow-list (`platform.inbound.allowed-clients`); otherwise `rejected` `NOT_ALLOWED_CLIENT`.
4. **Platform receiving from a tool:** also checks that the calling tool has an active subscription for the organization in `tenantRef`; otherwise `rejected` `NO_ACTIVE_SUBSCRIPTION_FOR_TENANT`.
5. **Tool receiving from the platform:** only the platform's client is allowed; the call acts as a system principal, never as a person; no user row is created for it.
6. Secrets (client secrets, database credentials) come from environment variables or a secrets manager. They are never in messages, logs or this repository.

## 8. Ordering, idempotency and reconcile

- **Receiver apply rule** (one transaction): if `eventId` is in the inbox → `duplicate`; if `version ≤ lastVersion(aggregate)` → `duplicate`; else apply, store `eventId`, store the new `lastVersion`, answer `applied`.
- **Order:** the platform delivers events of one aggregate in order; receivers must still tolerate disorder (the version rule).
- **Deletes** keep a tombstone (`deleted = true`, last version) so a late older upsert cannot bring the object back.
- **Reconcile:** the platform calls `get_state_digest`; where count or hash differ it calls `list_aggregate_versions`, then resends every aggregate whose version is missing or older. A tool never resends to the platform; it reports facts only through section 5.

## 9. Worked examples

**Fan-out of a user rename** — platform → tool `upsert_user`:
```json
{ "contractVersion": "1", "eventId": "3b0c…", "eventType": "UserUpserted", "occurredAt": "2026-10-09T11:00:00.000+05:30",
  "aggregateType": "User", "aggregateId": "36401029-823c-4cce-994a-877b69b4255d", "version": 4, "tenantRef": "100",
  "payload": { "sub": "36401029-823c-4cce-994a-877b69b4255d", "platformUserId": "501", "email": "arun@abcmanufacturing.com",
               "emailVerified": true, "firstName": "Arun", "lastName": "Kumar", "status": "ACTIVE" } }
```
Result: `{ "status": "applied", "version": 4 }`. Sending it again: `{ "status": "duplicate", "version": 4 }`.

**Registration inside a tool** — tool → platform `report_user_created`:
```json
{ "idempotencyKey": "b6f5…", "tenantRef": "100", "sub": "36401029-823c-4cce-994a-877b69b4255d",
  "email": "arun@abcmanufacturing.com", "emailVerified": true, "firstName": "Arun", "lastName": "Kumar" }
```
Result: `{ "status": "applied", "data": { "user": { …3.3, "version": 1 }, "membership": { …3.4, "version": 1 } } }`.

**Subscription ending** — platform → tool `set_subscription`:
```json
{ "aggregateType": "Subscription", "aggregateId": "9021", "version": 6, "tenantRef": "100",
  "payload": { "productCode": "thittam", "plan": "Pro Yearly", "status": "ACTIVE",
               "startsAt": "2025-11-01T00:00:00.000+05:30", "endsAt": "2026-10-31T23:59:00.000+05:30", "seats": 50 } }
```
At `2026-11-01T00:00:00.000+05:30` every tool denies access for this subscription, even if no further message has arrived.
