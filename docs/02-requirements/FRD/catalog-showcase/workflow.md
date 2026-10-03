# Workflow — Catalog Showcase

No new lifecycle. A platform's catalog visibility is a combination of two settings:

```mermaid
stateDiagram-v2
    [*] --> Visible: ACTIVE + show in catalog (default)
    Visible --> Hidden: show in catalog off
    Hidden --> Visible: show in catalog on
    Visible --> Inactive: status INACTIVE
    Hidden --> Inactive: status INACTIVE
    Inactive --> Visible: status ACTIVE (+ shown)
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| Any | Any | Platform administrator | BR-CAT-301 | None beyond the existing platform update |

App status keeps the [product-lifecycle](../product-lifecycle/workflow.md) workflow (Active / Inactive / Retired).
