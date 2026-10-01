# Workflow — Entitlements (REQ-SUB-002)

## Derivation flow (every check, no stored state)
```mermaid
flowchart TD
    A[Check: customer/organization + product<br/>+ optional feature key] --> B[Find subscriptions<br/>for that owner and product]
    B --> C{Any subscription with<br/>status = ACTIVE?}
    C -- No --> D[Not allowed<br/>no limits]
    C -- Yes --> E[Read that subscription's plan:<br/>includedFeatures, usageLimit]
    E --> F{Feature key given?}
    F -- No --> G[Allowed = true<br/>Return plan, includedFeatures, usageLimit]
    F -- Yes --> H{Feature key listed in<br/>includedFeatures?}
    H -- Yes --> G
    H -- No --> D
```
Nothing here is written or cached — the same computation runs on every call, directly against the subscription's live status and its plan's live fields. There is no grant/revoke step and nothing to keep in sync.

## Actors
| Step | Actor |
|---|---|
| View own entitlements | Customer |
| View organization entitlements | Organization member, per existing organization permissions |
| Call the internal check | Hosted product backend, via the internal SSO bridge secret |
| Call the customer-facing check | Platform UI (My products, organization subscriptions view) |
