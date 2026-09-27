# UI requirements — Tenant Lifecycle

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Edit organization (Tenant lifecycle section) | `/admin/registrations` (Organizations tab, Edit dialog) | Platform admin | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Region | Select, from `platformAdministrationApi.listRegions()` | No | "Not assigned" clears it | Backend message shown as-is |
| Allow seats beyond the licensed limit | Checkbox | No | n/a | n/a |

## States
- Empty: the region select shows "Not assigned" when the organization has no region, and when the region list hasn't loaded yet or is empty.
- Loading/Error: shares the existing organization-edit dialog's single save spinner/alert.

## Accessibility and localization
- Plain MUI `TextField`/`Checkbox`, same untranslated admin surface as the rest of the organization-edit dialog — no new gap introduced.
