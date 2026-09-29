# Workflow — Subscription Lifecycle

## States
```mermaid
stateDiagram-v2
    [*] --> PENDING_SUBSCRIPTION
    PENDING_SUBSCRIPTION --> ACTIVE: subscribe (existing, prior sprint)
    ACTIVE --> SUSPENDED: suspend (BR-SUB-001)
    SUSPENDED --> ACTIVE: reactivate (BR-SUB-002)
    ACTIVE --> EXPIRED: expiry job (BR-SUB-008)
    EXPIRED --> ACTIVE: renew (BR-SUB-006)
    ACTIVE --> CANCELLED: cancel (BR-SUB-003)
    SUSPENDED --> CANCELLED: cancel (BR-SUB-003)
    EXPIRED --> CANCELLED: cancel (BR-SUB-003)
    CANCELLED --> [*]
```
Change plan (BR-SUB-007) does not move between these states — it only changes `planId`, on any status except CANCELLED.

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| ACTIVE | SUSPENDED | Customer | BR-SUB-001 | `SUBSCRIPTION_SUSPENDED` audit; notification |
| SUSPENDED | ACTIVE | Customer | BR-SUB-002 | `SUBSCRIPTION_REACTIVATED` audit; notification |
| ACTIVE / SUSPENDED / EXPIRED / PENDING_SUBSCRIPTION | CANCELLED | Customer | BR-SUB-003 | `SUBSCRIPTION_CANCELLED` audit; notification |
| ACTIVE | EXPIRED | Scheduled job | BR-SUB-008 (`expiresAt` passed) | `SUBSCRIPTION_EXPIRED` audit; notification |
| Any non-CANCELLED | (same status, `expiresAt` extended) | Customer | BR-SUB-005 | `SUBSCRIPTION_RENEWED` audit; notification |
| EXPIRED | ACTIVE | Customer (via renew) | BR-SUB-006 | `SUBSCRIPTION_RENEWED` audit; notification |
| Any non-CANCELLED | (same status, `planId` changed) | Customer | BR-SUB-007 | `SUBSCRIPTION_PLAN_CHANGED` audit |
