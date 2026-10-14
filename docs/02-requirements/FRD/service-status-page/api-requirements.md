# API requirements — Interim Service Status Page

Paths are relative to `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/me/service-status` | Customer view | Signed-in user | 200 `ServiceStatusPageDto` | 401 |
| GET | `/admin/service-status` | Admin view (all active products, latest incidents) | `MANAGE_SERVICE_STATUS` | 200 `ServiceStatusPageDto` | 401, 403 |
| PUT | `/admin/service-status/products/{productId}` | Post a product's status; body `{"status": "DEGRADED", "note": "…"}` | `MANAGE_SERVICE_STATUS` | 200 `ProductStatusDto` | 400, 401, 403, 404 |
| POST | `/admin/service-status/incidents` | Post an incident (**C101:** a support incident; returns the administrator record `AdminIncidentDto`, see [incident-problem-management.md](../../../06-api/api-requirements/incident-problem-management.md)) | `MANAGE_SERVICE_STATUS` | 200 `AdminIncidentDto` | 400, 401, 403, 404 |
| PUT | `/admin/service-status/incidents/{id}` | Update or resolve an incident (**C101:** phases, internal fields) | `MANAGE_SERVICE_STATUS` | 200 `AdminIncidentDto` | 400, 401, 403, 404 |

## Request / response
Incident body (`IncidentRequest`; since C101 it also takes the optional internal fields `severity`, `phase`, `internalNotes`, `assignedToCustomerId`, `resolutionSummary`; the customer endpoint still returns only `IncidentDto`): `{"productId": 1, "title": "…", "message": "…", "startedAt": "2026-09-26T08:15:00Z", "endedAt": null}` (`endedAt` null = open).

`ServiceStatusPageDto`: `{"enabled": true, "products": [ProductStatusDto], "incidents": [IncidentDto]}`.
`ProductStatusDto`: `productId, productName, status, note, updatedAt, purchased, openIncidents`, and from C101 `source` (`INSTANCES` or `MANUAL`), `reportingInstances`, `downInstances`, `degradedInstances` ([service-instances.md](../../../06-api/api-requirements/service-instances.md#status-page-fields)).
`IncidentDto`: `id, productId, productName, title, message, startedAt, endedAt, open`.

Frontend client: `serviceStatusApi`, `adminServiceStatusApi` in `frontend/src/api/serviceStatusApi.ts`. Tables: `product_service_status`, `service_incident` (migration V003).
