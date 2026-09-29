# UI requirements — Interim Service Status Page

Placement decided in [C26](../../../01-business/roadmap/open-decisions.md#c26).

## Screens
| Screen | Route | Roles |
|---|---|---|
| Status page (customer view) | `/status`, "Service status" in the signed-in sidebar; linked from the dashboard's Service health card | Every signed-in user |
| Status and incident posting (admin view) | `/admin/service-status`, "Service status" in the Administration section | `MANAGE_SERVICE_STATUS` |

## Fields and validation (admin)
| Field | Type | Required | Validation |
|---|---|---|---|
| Product | Select of active products | Yes | Backend: active product |
| Status value | Select of the five values | Yes | Backend |
| Note | Text, max 500 | No | Backend |
| Incident title | Text, max 200 | Yes | Backend |
| Incident message | Text, max 4000 | Yes | Backend |
| Start time | Date and time | Yes | Backend |
| End time | Date and time | No (empty = open) | Backend: not before start |

Backend messages are shown as returned.

## States
- Customer view: a table of products with a colour-coded status chip; incidents for purchased products, open ones marked amber, resolved ones green. For a product the customer has not purchased that has open incidents, a note says incident details are for purchased products.
- Setting off: the customer page shows "The service status page is turned off." and no status; the admin page shows a notice and keeps working.
- Admin: "Resolve now" pre-fills the end time with the current time.

## Accessibility and localization
Text under `serviceStatus` in `en.json` and `es.json`. jest-axe checks in `ServiceStatusPage.test.tsx`.
