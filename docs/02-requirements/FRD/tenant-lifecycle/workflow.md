# Workflow — Tenant Lifecycle

No new state machine — this extends REQ-TEN-001's existing organization-edit flow (see `organization-lifecycle`'s own workflow) with two more fields. Neither field has states of its own beyond "set" / "not set" (region) and "on" / "off" (seat overage).

## Transitions
| From | To | Actor | Condition / rule | Side effects (notifications, audit) |
|------|----|-------|------------------|-------------------------------------|
| Any region (or none) | Another region (or none) | Admin | BR-TEN-020 | Recorded as part of the existing `ORGANIZATION_UPDATED` audit entry |
| allowSeatOverage off | on (or back) | Admin | BR-TEN-023 | Recorded as part of the existing `ORGANIZATION_UPDATED` audit entry |
