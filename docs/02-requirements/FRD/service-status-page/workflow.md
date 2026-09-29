# Workflow — Interim Service Status Page

## Incident
```mermaid
stateDiagram-v2
    [*] --> Open: admin posts incident (no end time)
    Open --> Resolved: admin sets an end time
    Resolved --> Open: admin clears the end time
```

## Page lifecycle
```mermaid
stateDiagram-v2
    [*] --> Enabled: app.status-page.enabled = true (default)
    Enabled --> Disabled: setting off
    Disabled --> Enabled: setting on
    Enabled --> StatusFromHealthMonitoring: sprint 2027.1.1 retirement task
    StatusFromHealthMonitoring --> Removed: sprint 2027.1.3 retirement task
```

## Transitions
| From | To | Actor | Condition / rule | Side effects |
|---|---|---|---|---|
| — | Open | Platform admin | `MANAGE_SERVICE_STATUS`; active product | Audit `INCIDENT_POSTED`; dashboard alert for purchasers |
| Open | Resolved | Platform admin | End time not before start | Audit `INCIDENT_UPDATED` |
| Enabled | Disabled | Operator (configuration) | Setting off | Customer view returns `enabled: false` |
