# Workflow — Interim Service Status Page

## States
```mermaid
stateDiagram-v2
    [*] --> Enabled: configuration setting on
    Enabled --> Disabled: configuration setting off
    Disabled --> Enabled: configuration setting on
    Enabled --> StatusFromHealthMonitoring: sprint 2027.1.1 retirement task
    StatusFromHealthMonitoring --> Removed: sprint 2027.1.3 retirement task (incidents move to 12.04)
```

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|---|---|---|---|---|
| Enabled | Disabled | Operator (configuration) | Setting switched off | Not specified |
| Enabled | Status from Health Monitoring | Development team | Sprint 2027.1.1 retirement task | Not specified |
| Status from Health Monitoring | Removed | Development team | Sprint 2027.1.3 retirement task | Not specified |
