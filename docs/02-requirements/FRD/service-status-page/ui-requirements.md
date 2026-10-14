# UI requirements — Interim Service Status Page

Placement decided in [C26](../../../01-business/roadmap/open-decisions.md#c26).

## Screens
| Screen | Route | Roles |
|---|---|---|
| Status page (customer view) | `/status`, "Service status" in the signed-in sidebar; linked from the dashboard's Service health card | Every signed-in user |
| Status and incident posting (admin view) | `/admin/service-status`, "Service status" in the Administration section. **Since C80 / C101** the one entry is **Platform → Service status** (`/status`); administrators get the tabs `Status \| Manage \| Instances \| Incidents \| Problems` (`?tab=`) | `MANAGE_SERVICE_STATUS` |

## Changes of C101 (2026-10-14)
- The **Manage tab posts product status only**; the incident form moved to the **Incidents** tab ([incident-problem-management.md](../../../05-ui/screen-requirements/incident-problem-management.md)), where an incident has public and internal fields. The incident rows of the table below therefore describe the public fields now entered on the Incidents tab.
- A product whose status is derived from instances shows **"Based on N reporting instances (x down, y degraded)"** ([service-instances.md](../../../05-ui/screen-requirements/service-instances.md#3-status-page-caption)). A manually set status shows no caption.

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
