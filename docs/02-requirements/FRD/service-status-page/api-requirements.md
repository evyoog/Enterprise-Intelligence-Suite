# API requirements — Interim Service Status Page

Paths are relative to `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/me/service-status` | Customer view | Signed-in user | 200 `ServiceStatusPageDto` | 401 |
| GET | `/admin/service-status` | Admin view (all active products, latest incidents) | `MANAGE_SERVICE_STATUS` | 200 `ServiceStatusPageDto` | 401, 403 |
| PUT | `/admin/service-status/products/{productId}` | Post a product's status; body `{"status": "DEGRADED", "note": "…"}` | `MANAGE_SERVICE_STATUS` | 200 `ProductStatusDto` | 400, 401, 403, 404 |
| POST | `/admin/service-status/incidents` | Post an incident | `MANAGE_SERVICE_STATUS` | 200 `IncidentDto` | 400, 401, 403, 404 |
| PUT | `/admin/service-status/incidents/{id}` | Update or resolve an incident | `MANAGE_SERVICE_STATUS` | 200 `IncidentDto` | 400, 401, 403, 404 |

## Request / response
Incident body (`IncidentRequest`): `{"productId": 1, "title": "…", "message": "…", "startedAt": "2026-09-26T08:15:00Z", "endedAt": null}` (`endedAt` null = open).

`ServiceStatusPageDto`: `{"enabled": true, "products": [ProductStatusDto], "incidents": [IncidentDto]}`.
`ProductStatusDto`: `productId, productName, status, note, updatedAt, purchased, openIncidents`.
`IncidentDto`: `id, productId, productName, title, message, startedAt, endedAt, open`.

Frontend client: `serviceStatusApi`, `adminServiceStatusApi` in `frontend/src/api/serviceStatusApi.ts`. Tables: `product_service_status`, `service_incident` (migration V003).
