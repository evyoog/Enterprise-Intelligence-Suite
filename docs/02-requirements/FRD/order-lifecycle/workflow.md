# Workflow — Order Lifecycle

## States
```mermaid
stateDiagram-v2
    [*] --> SUBMITTED: submitOrder BR-ORD-001, one or several products, optional reason
    [*] --> APPROVED: direct purchase from the cart BR-ORD-010
    SUBMITTED --> APPROVED: approveOrder (BR-ORD-002) — provisions synchronously
    SUBMITTED --> REJECTED: rejectOrder (BR-ORD-003)
    SUBMITTED --> CANCELLED: cancelOrder (BR-ORD-004)
    APPROVED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```
No PROVISIONED state — APPROVED already means provisioned (see this feature's own requirement.md). Every non-SUBMITTED status is terminal.

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| (new) | SUBMITTED | Any active member (from the cart: one request holding the whole cart, with a reason) | BR-ORD-001, BR-ORD-007, BR-ORD-008 | `ORDER_SUBMITTED` audit; notifies every ACTIVE ORG_ADMIN with the products and the reason |
| (new) | APPROVED | Organization admin or Manage billing holder, from the cart | BR-ORD-010 | Direct purchase: every product provisioned and one invoice in the same action; `ORDER_PURCHASED_DIRECTLY` audit; admins notified when the buyer is not an admin |
| SUBMITTED | APPROVED | ORG_ADMIN (MANAGE_ORDERS) | BR-ORD-002, BR-ORD-009 | Provisions the organization's subscription to every product and creates one invoice; `ORDER_APPROVED` audit; notifies the requester |
| SUBMITTED | REJECTED | ORG_ADMIN (MANAGE_ORDERS) | BR-ORD-003 | `ORDER_REJECTED` audit; notifies the requester |
| SUBMITTED | CANCELLED | The requester | BR-ORD-004 | `ORDER_CANCELLED` audit |
