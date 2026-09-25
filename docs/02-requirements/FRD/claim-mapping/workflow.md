# Workflow — Configurable Claim Mapping

## States
```mermaid
stateDiagram-v2
    [*] --> DefaultMapping: provider created
    DefaultMapping --> CustomMapping: administrator sets a mapping
    CustomMapping --> DefaultMapping: administrator clears the mapping (Not specified)
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|---|---|---|---|---|
| Default mapping | Custom mapping | Organization administrator | Not specified | Not specified |
