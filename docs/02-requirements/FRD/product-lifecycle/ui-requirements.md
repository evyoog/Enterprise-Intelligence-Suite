# UI requirements — Product Lifecycle & Structure

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Edit app | `/admin/products/:id/edit` | Platform admin (`MANAGE_CATALOG`) | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Revision (display only; C66 label "Revision N") | Text with tooltip, read-only | n/a | n/a | n/a |
| Status | Chip + Publish/Retire buttons | n/a | Retire hidden when already RETIRED; Publish hidden when already ACTIVE | Backend error message shown as-is |
| Parent product | Select | No | Cannot select itself | n/a |
| Variant label | Text | No | Free text, only meaningful with a parent | n/a |
| Depends on | Multi-select | No | Cannot include itself | Backend 400 message shown as-is |

## States
- Empty: parent/depends-on selects show "None — top-level" and an empty multi-select until the admin product list loads.
- Loading: the existing product-load spinner covers the whole form; the Publish/Retire buttons are disabled while a status change is in flight.
- Error: the backend's own message is shown in an inline alert (e.g. a 409 delete refusal, a 400 self-dependency).

## Accessibility and localization
- Publish/Retire buttons and the revision label ("Revision N", formerly a "Version N" chip — renamed by [C66](../../../01-business/roadmap/open-decisions.md#c66) because it is an edit counter, not a release version) are plain MUI components, inheriting the page's existing keyboard/focus and screen-reader behavior. Retire asks for confirmation (C66).
- These are admin-only fields; not yet run through the i18n `t()` catalog like customer-facing text (existing admin form fields are also plain English strings today, not translated — no new gap introduced).
