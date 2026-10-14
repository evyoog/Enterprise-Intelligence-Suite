# Workflow — Service instances (REQ-SRM-001)

## From subscription to the status page
```mermaid
sequenceDiagram
    autonumber
    participant C as Customer / organization
    participant S as Subscription flow
    participant I as Service instance (EIS)
    participant A as Platform administrator
    participant P as Hosted product
    participant SP as Status page

    C->>S: subscription becomes ACTIVE
    S->>I: create instance (PROVISIONING, health UNKNOWN)
    A->>I: create reporter key for the product (shown once)
    A-->>P: hands over the key (outside EIS)
    loop about every minute
        P->>I: POST /product-reports/health  (X-Reporter-Key)
        I->>I: check key, product, health value
        I-->>P: accepted / rejected per report
        Note over I: first report: PROVISIONING -> ACTIVE
    end
    SP->>I: product status? (instances that reported in the last 5 minutes)
    I-->>SP: Operational / Degraded / Partial / Major outage, with the counts
    Note over SP: a manual Maintenance wins; no recent report keeps the manual status
    A->>I: product cannot report: set status/health by hand with a note (audited)
```

## Instance status
```mermaid
stateDiagram-v2
    [*] --> PROVISIONING: subscription ACTIVE (instance created)
    PROVISIONING --> ACTIVE: first report from the product
    ACTIVE --> FAILED: product reports FAILED, or an administrator sets it
    FAILED --> ACTIVE: product reports ACTIVE, or an administrator sets it
    PROVISIONING --> SUSPENDED: subscription suspended
    ACTIVE --> SUSPENDED: subscription suspended
    FAILED --> SUSPENDED: subscription suspended
    SUSPENDED --> ACTIVE: subscription ACTIVE again (it has reported before)
    SUSPENDED --> PROVISIONING: subscription ACTIVE again (it never reported)
    PROVISIONING --> DEPROVISIONED: subscription cancelled or expired
    ACTIVE --> DEPROVISIONED: subscription cancelled or expired
    FAILED --> DEPROVISIONED: subscription cancelled or expired
    SUSPENDED --> DEPROVISIONED: subscription cancelled or expired
    DEPROVISIONED --> ACTIVE: subscription ACTIVE again (it has reported before)
    DEPROVISIONED --> PROVISIONING: subscription ACTIVE again (it never reported)
```
An administrator can set any status by hand (BR-SRM-009); that is not drawn.

## Health on the status page
```mermaid
flowchart TD
    A[Product on the status page] --> M{Manual status is Maintenance?}
    M -- Yes --> MM[Maintenance, source MANUAL]
    M -- No --> R{Any ACTIVE or FAILED instance with a report in the last 5 minutes and health not UNKNOWN?}
    R -- No --> N[Status an administrator set by hand, or Operational; source MANUAL]
    R -- Yes --> D{Down instances d of n reporting}
    D -- "d x 2 > n" --> MJ[Major outage]
    D -- "1 <= d, d x 2 <= n" --> PO[Partial outage]
    D -- "d = 0" --> G{Any Degraded?}
    G -- Yes --> DG[Degraded]
    G -- No --> OP[Operational]
```

## Transitions
| From | To | Actor | Condition / rule | Side effects |
|---|---|---|---|---|
| (none) | PROVISIONING | Subscription flow | Subscription is ACTIVE; BR-SRM-001 | Recorded as `SYSTEM`; none audited |
| PROVISIONING | ACTIVE | Product | First accepted report; BR-SRM-007 | Health and time of report stored; `PRODUCT` |
| any | SUSPENDED / DEPROVISIONED | Subscription flow, reconcile job | BR-SRM-002 | `SYSTEM` |
| SUSPENDED / DEPROVISIONED | PROVISIONING / ACTIVE | Subscription flow, reconcile job | Subscription ACTIVE again; BR-SRM-002 | `SYSTEM` |
| any | any | Platform administrator | Note required; BR-SRM-009 | Audit `SERVICE_INSTANCE_CHANGED`; `ADMIN` |
| ACTIVE / PROVISIONING | FAILED, ACTIVE | Product | Status in the report; BR-SRM-006 | `PRODUCT` |
