# UI requirements — Interim Service Status Page

Screens and placement follow the sprint 2026.3.3 development plan: reuse existing pages, layouts and components. Do not add a new layout.

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Status page (customer view) | Not specified | Customers (visibility Not specified) | Not specified |
| Status and incident posting (admin view) | Under the existing `/admin` layout (exact path Not specified) | Platform administrator | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|---|---|---|---|---|
| Product | Not specified | Not specified | Not specified | Not specified |
| Status value | Not specified (values Not specified) | Not specified | Not specified | Not specified |
| Incident title | Not specified | Not specified | Not specified | Not specified |
| Incident message | Not specified | Not specified | Not specified | Not specified |
| Start and end time | Not specified | Not specified | Not specified | Not specified |
| Visibility | Not specified | Not specified | Not specified | Not specified |

Validation is done by the backend. The UI shows the backend's error message as returned and does not re-implement the rules.

## States
- The field list is from [C20](../../../01-business/roadmap/open-decisions.md#c20). Types, rules and states are to be defined in this FRD before approval.
- When the configuration setting is off, the page is not shown.

## Accessibility and localization
- All user-facing text goes through i18n, in both `en.json` and `es.json`.
- Keyboard operable, and screen-reader labelled controls (workbook function 01.01.02). A jest-axe check runs in the component tests.
