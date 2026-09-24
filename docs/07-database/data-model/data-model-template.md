<!--
DATA MODEL TEMPLATE
File naming convention: DE-<NNN>-<entity-name>.md, or one file per Design if entities are design-specific.
Cross-check new entities against the platform's Core Domain Entities list before creating a new one.
-->

# DE-<NNN>: <Entity Name>

| Field | Value |
|---|---|
| Entity ID (required) | DE-<NNN> |
| Domain | <e.g. Catalog, Commerce, Identity> |
| Owning Application | |
| Owning Design | DES-<APP-CODE>-<NNN> |

## Definition
<One or two sentences describing what this entity represents.>

## Attributes
| Attribute | Type | Required | Description |
|---|---|---|---|
| id | string (UUID) | Yes | Primary identifier |
| tenant_id | string | Yes | Tenant this record belongs to — required on every multi-tenant entity |
| | | | |

## Key Relationships
| Related Entity | Relationship | Notes |
|---|---|---|
| <Entity> | <owns / belongs to / references> | |

## Lifecycle / States
<If this entity has a state machine, list states and valid transitions. Otherwise state "No formal lifecycle.">

## Data Classification & Residency
<Public / Internal / Confidential / Restricted. Any residency constraint (e.g. "must remain in customer's configured region").>

## Events Emitted
<List domain events raised when this entity changes, e.g. EVT-xxx EntityCreated/Updated/Deleted.>

## Retention Policy
<How long is this data kept? Any regulatory driver?>
