# Workflow — <Feature Name>

## States
```mermaid
stateDiagram-v2
    [*] --> Draft
    Draft --> Submitted
    Submitted --> Approved
    Submitted --> Rejected
    Approved --> [*]
    Rejected --> [*]
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| Draft | Submitted | <actor> | <BR-…> | <…> |
