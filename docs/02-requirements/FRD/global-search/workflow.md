# Workflow — Global Search

No state machine — every call is a stateless read across three existing, independent sources; nothing here is created, transitioned, or persisted.

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| n/a | n/a | Any visitor / customer | BR-PRT-001–.005 | None — a pure read, not audited |
