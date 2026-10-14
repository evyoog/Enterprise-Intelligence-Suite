# Workflow — Interim Service Status Page

## Incident
```mermaid
stateDiagram-v2
    [*] --> Open: admin posts incident (no end time)
    Open --> Resolved: admin sets an end time
    Resolved --> Open: admin clears the end time
```

## Incident (from C101)
Since 2026-10-14 an incident has the phases Logged, Investigating, Resolved and Closed ([REQ-SUP-003 workflow](../incident-problem-management/workflow.md)). The customer view still distinguishes only open (no end time) from resolved.

## Product status (from C101)
A product's status is derived from its instances' health, with a manual Maintenance winning and the manual status kept when no instance reported recently ([REQ-SRM-001 workflow](../service-instances/workflow.md#health-on-the-status-page)).

## Page lifecycle
```mermaid
stateDiagram-v2
    [*] --> Enabled: app.status-page.enabled = true (default)
    Enabled --> Disabled: setting off
    Disabled --> Enabled: setting on
    Enabled --> StatusFromHealthMonitoring: status derived from instances (built 2026-10-14, C101)
    StatusFromHealthMonitoring --> Removed: sprint 2027.1.3 retirement task
```

## Transitions
| From | To | Actor | Condition / rule | Side effects |
|---|---|---|---|---|
| — | Open | Platform admin | `MANAGE_SERVICE_STATUS`; active product | Audit `INCIDENT_POSTED`; dashboard alert for purchasers |
| Open | Resolved | Platform admin | End time not before start | Audit `INCIDENT_UPDATED` |
| Enabled | Disabled | Operator (configuration) | Setting off | Customer view returns `enabled: false` |
