# Workflow — Member Role Assignment

## States
```mermaid
stateDiagram-v2
    MEMBER --> ORG_ADMIN: administrator assigns ORG_ADMIN
    ORG_ADMIN --> MEMBER: administrator assigns MEMBER (not the last active ORG_ADMIN)
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|---|---|---|---|---|
| MEMBER | ORG_ADMIN | Organization administrator | `MANAGE_USERS`; same organization | Target notified; audit `MEMBER_ROLE_CHANGED` |
| ORG_ADMIN | MEMBER | Organization administrator | `MANAGE_USERS`; same organization; at least one other active `ORG_ADMIN` remains | Target notified; audit `MEMBER_ROLE_CHANGED` |
