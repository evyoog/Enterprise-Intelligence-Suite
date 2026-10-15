# Workflow — Application access requests (REQ-TEN-010)

## (a) Request → notify → approve/reject → grant

```mermaid
stateDiagram-v2
    [*] --> PENDING: request() BR-AAC-001, BR-AAC-002, BR-AAC-003
    PENDING --> APPROVED: approve() BR-AAC-005
    PENDING --> REJECTED: reject() BR-AAC-006
    PENDING --> CANCELLED: cancel() BR-AAC-004
    APPROVED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```
No PENDING request is ever auto-approved or auto-rejected (BR-AAC-007); every non-PENDING status is terminal — there is no reopening a decided request.

```mermaid
sequenceDiagram
    autonumber
    actor Member
    participant EIS as EIS backend
    participant Admin as Org admin / Manage product access holder
    participant Audit as Audit log

    Member->>EIS: POST /organization/me/access-requests {productId, message}
    EIS->>EIS: Active subscription? Not already access? No pending request? (BR-AAC-001/002/003)
    EIS->>Audit: ACCESS_REQUESTED
    EIS-->>Admin: Notify "New access request"
    EIS-->>Member: AccessRequestDto (PENDING)

    alt Admin approves
        Admin->>EIS: POST /organization/me/access-requests/{id}/approve
        EIS->>EIS: AccessSyncService#grantProduct (seat check, BR-AAC-005)
        EIS->>Audit: ACCESS_REQUEST_APPROVED
        EIS-->>Member: Notify "You can now open {product}"
    else Admin rejects
        Admin->>EIS: POST /organization/me/access-requests/{id}/reject {note}
        EIS->>EIS: note required (BR-AAC-006)
        EIS->>Audit: ACCESS_REQUEST_REJECTED
        EIS-->>Member: Notify "Access request declined: {note}"
    else Member cancels first
        Member->>EIS: POST /organization/me/access-requests/{id}/cancel
        EIS->>EIS: Only while PENDING (BR-AAC-004)
    end
```

## (b) Self-removal — a separate, immediate flow

```mermaid
flowchart LR
    A[Member: Remove my access] --> B{Has ACTIVE access?}
    B -- no --> X[Refused: you do not have access to this product]
    B -- yes --> C[AccessSyncService#revokeProduct — seat released at once]
    C --> D[Audit: ACCESS_SELF_REMOVED]
    D --> E[Organization subscription unchanged]
    F[Member wants it back later] --> G[Must raise a new request — no reinstatement]
```

## (c) Order reminder — unrelated table, same sprint

```mermaid
flowchart LR
    A[OrderReminderJob, hourly] --> B[OrderService.remindPendingOrders]
    B --> C{Order SUBMITTED and\ncreated >= 3 days ago\nand remindedAt is null?}
    C -- no --> Z[Skip]
    C -- yes --> D[Set remindedAt = now]
    D --> E[Notify every ACTIVE ORG_ADMIN]
    E --> F[Order status unchanged — never auto-approved or auto-rejected]
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| (new) | PENDING | Active member | BR-AAC-001, BR-AAC-002, BR-AAC-003 | `ACCESS_REQUESTED` audit; notifies org admins / *Manage product access* holders |
| PENDING | APPROVED | Org admin / *Manage product access* holder | BR-AAC-005, BR-AAC-007 | Grants access (seat check); `ACCESS_REQUEST_APPROVED` audit; notifies requester |
| PENDING | REJECTED | Org admin / *Manage product access* holder | BR-AAC-006, BR-AAC-007 | `ACCESS_REQUEST_REJECTED` audit; notifies requester with the reason |
| PENDING | CANCELLED | The requester | BR-AAC-004 | No notification; request becomes historical |
| ACTIVE access | INACTIVE (self-removal, not a request) | The member themself | BR-AAC-010 | `ACCESS_SELF_REMOVED` audit; seat released; organization subscription untouched |
| SUBMITTED order (≥3 days, unreminded) | SUBMITTED order (reminded) | `OrderReminderJob` (scheduled) | BR-AAC-012 | Notifies every ACTIVE `ORG_ADMIN`; `remindedAt` set; status never changes |
