# Workflow — Platform ↔ tool synchronization

States of a delivery (platform → one tool): `PENDING` → `DELIVERED` | `RETRYING` → `FAILED` → (retry/replay) → `PENDING`.
States of a tenant for an organization and tool: `PENDING` → `READY` | `FAILED`; `READY` ⇄ `SUSPENDED`.

## F1 Platform change fans out
```mermaid
sequenceDiagram
  participant A as Admin (platform UI)
  participant P as Platform service
  participant O as Outbox
  participant D as Dispatcher
  participant T as Tool (MCP)
  A->>P: change (e.g. move org node)
  P->>P: commit change, bump version, write outbox row (one transaction)
  D->>O: pick pending event
  D->>D: find tools with active subscription + READY tenant
  D->>T: upsert_org_node(envelope)
  T->>T: inbox check, version check, apply (one transaction)
  T-->>D: applied | duplicate | retry | rejected
  D->>O: record receipt per tool
```

## F2 Edit made inside a tool (write-through)
```mermaid
sequenceDiagram
  participant U as User (tool UI)
  participant T as Tool service
  participant P as Platform MCP
  U->>T: edit shared field
  T->>P: update_user_profile(idempotencyKey, changes)
  P->>P: validate, commit, bump version
  P-->>T: snapshot + version
  T->>T: apply snapshot (same path as F1)
  P->>P: fan out to every other tool (F1)
  Note over T: platform unreachable: refuse, write nothing locally
```

## F3 Registration inside a tool
```mermaid
sequenceDiagram
  participant U as Person
  participant T as Tool
  participant K as Keycloak
  participant P as Platform MCP
  U->>T: register
  T->>K: find user by email
  alt exists
    K-->>T: sub
  else new
    T->>K: create user (real email)
    K-->>T: sub
  end
  T->>P: report_user_created(idempotencyKey, sub, email, name, tenantRef)
  alt platform ok
    P->>P: link or create customer + membership by sub
    P-->>T: snapshot
    T->>T: store local user (no product access, no business records)
  else platform fails
    T->>K: disable the user just created
    T-->>U: could not complete, try again
  end
```

## F4 Subscription or access change
Platform writes the change and a `SubscriptionChanged` / `UserProductAccessGranted|Revoked` event (version bumped) → F1 delivery → tool updates `platform_subscription` / `platform_user_access` and clears its entitlement cache → the next request is checked against the new data.

## F5 Provisioning
```mermaid
sequenceDiagram
  participant P as Platform
  participant T as Tool
  P->>P: subscription starts; registry entry PENDING
  P->>T: provision_tenant(tenantRef, organization, firstAdmin, schemaVersion)
  T->>T: resolve datasource from its registry, create/verify schema, migrate, create root + admin
  T-->>P: status READY, schemaVersion
  P->>P: registry READY
  P->>T: upsert_* for hierarchy, users, subscription, access (F1)
```
A failure leaves the entry `FAILED` with the error; the next identical call resumes. Nothing is deleted.

## F6 Request check in a tool
1. Token valid (signature, issuer, audience, expiry). 2. Tenant resolved through the registry; the user must exist in that tenant. 3. User active. 4. Organization active. 5. Subscription effective now. 6. Product access active. 7. Tenant READY. 8. Tool permission for the action. 9. Sensitive action → ask the platform; no answer → deny.

## F7 Reconcile
Every interval the platform asks each tool for a digest per aggregate type (count and hash of `(id, version)`), compares with its own, asks for `(id, version)` pairs where they differ, and resends what is missing or older.
