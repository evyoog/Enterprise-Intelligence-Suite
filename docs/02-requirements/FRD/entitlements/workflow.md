# Workflow — Entitlements (REQ-SUB-002)

## Derivation flow (every check, no stored state)
```mermaid
flowchart TD
    A[Check: customer and/or organization + product<br/>+ optional feature key] --> A1{Customer or organization given,<br/>and product ACTIVE in the catalogue?}
    A1 -- No --> X[Not allowed<br/>BAD_REQUEST or UNKNOWN_PRODUCT]
    A1 -- Yes --> B[Find subscriptions<br/>for that owner and product]
    B --> C{Any subscription ACTIVE<br/>and not past its term end?}
    C -- No --> D[Not allowed<br/>NO_SUBSCRIPTION, no limits]
    C -- Yes --> M{Organization subscription for a member,<br/>and member has ACTIVE access to the product?}
    M -- No --> N[Not allowed<br/>NO_MEMBER_ACCESS]
    M -- Yes --> E[Read that subscription's plan:<br/>includedFeatures, usageLimit]
    E --> F{Feature key given?}
    F -- No --> G[Allowed = true<br/>Return plan, ownerType, expiresAt,<br/>includedFeatures, usageLimit]
    F -- Yes --> H{Feature key listed in<br/>includedFeatures?}
    H -- Yes --> G
    H -- No --> I[Not allowed<br/>FEATURE_NOT_INCLUDED<br/>plan and limit still returned]
```
Nothing here is written or cached — the same computation runs on every call, directly against the subscription's live status and its plan's live fields. There is no grant/revoke step and nothing to keep in sync.

## Actors
| Step | Actor |
|---|---|
| View own entitlements | Customer |
| View organization entitlements | Organization member, per existing organization permissions |
| Call the internal check | Hosted product backend, via the internal SSO bridge secret |
| Call the customer-facing check | Platform UI (My products, organization subscriptions view) |
| Support lookup | Platform administrator with `VIEW_ENTITLEMENTS` (read-only; audited as `ENTITLEMENTS_VIEWED`) |

## Administrator lookup ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-3)
```mermaid
sequenceDiagram
    participant A as Platform admin (VIEW_ENTITLEMENTS)
    participant UI as /admin/entitlements
    participant API as GET /admin/entitlements/{customers|organizations}/{id}
    participant S as EntitlementService
    participant L as Audit log
    A->>UI: choose Customer or Organization, enter the numeric ID
    UI->>API: request
    API->>S: look up (403 without the permission)
    S->>S: 404 if the customer or organization does not exist
    S->>L: ENTITLEMENTS_VIEWED (admin, target, time)
    S-->>UI: the entitlements derived now
    UI-->>A: cards (read-only), or "Nothing is active…"
```
