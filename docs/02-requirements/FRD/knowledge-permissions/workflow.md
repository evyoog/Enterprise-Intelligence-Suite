# Workflow — Knowledge permissions

```mermaid
flowchart LR
  PA[Platform admin] -->|creates role with permission| R[Platform role]
  R -->|assigned to a person: Not specified, open question 3| P[Person]
  P -->|calls a knowledge write endpoint| API[Backend]
  API -->|permission held| OK[Action + audit]
  API -->|permission missing| F[403 + audit failure]
```

| From | To | Actor | Condition | Side effects |
|---|---|---|---|---|
| No knowledge permission | Contributor | Platform admin | Role with `KNOWLEDGE_CONTRIBUTE` assigned | Audit `KNOWLEDGE_PERMISSION_GRANTED` |
| Contributor | Publisher | Platform admin | Role with `MANAGE_KNOWLEDGE_BASE` assigned | Audit |
| Any | No knowledge permission | Platform admin | Role removed | Audit `KNOWLEDGE_PERMISSION_REMOVED` |
