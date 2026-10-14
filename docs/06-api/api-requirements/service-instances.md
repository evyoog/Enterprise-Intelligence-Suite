# API requirements — Service instances

Requirement: [REQ-SRM-001](../../02-requirements/FRD/service-instances/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/service-instances/business-rules.md). Decision: [C101](../../01-business/roadmap/open-decisions.md#c101) (SR-1 to SR-5). Paths are relative to `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`. Data model: [service-instances-sla-incidents.md](../../07-database/data-model/service-instances-sla-incidents.md).

**Permissions.** The platform endpoints sit under `/admin/service-status/**`, which `SecurityConfig` gates with the platform permission `MANAGE_SERVICE_STATUS` (the same rule as the status page administration). The organization endpoint needs a signed-in user and the organization permission `MANAGE_ORGANIZATION`, checked in `ServiceInstanceService#listForOrganization`. The product endpoint is **open at the URL level** (`POST /product-reports/**` is `permitAll`, like the webhooks) and is authenticated by the **`X-Reporter-Key`** header inside `ServiceInstanceService#report`.

## Platform administrators (`MANAGE_SERVICE_STATUS`)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/service-status/instances` | Every service instance, ordered by product then id. Optional filters `productId`, `status`, `health` (case-insensitive; `health` is the effective health, so an instance with no recent report matches `UNKNOWN`) | 200 `Instance[]` | 401, 403 |
| GET | `/admin/service-status/instances/summary` | Counts of the `ACTIVE` and `PROVISIONING` instances, optionally of one `productId` | 200 `Summary` | 401, 403 |
| GET | `/admin/service-status/instances/{id}` | One instance | 200 `Instance` | 401, 403, 404 "Service instance not found" |
| PUT | `/admin/service-status/instances/{id}` | Set status and/or health by hand ([BR-SRM-009](../../02-requirements/FRD/service-instances/business-rules.md)); body `ManualChange`; audited `SERVICE_INSTANCE_CHANGED` | 200 `Instance` | 400 (no note; neither status nor health; unknown value), 401, 403, 404 |
| GET | `/admin/service-status/reporter-keys` | Every reporter key, newest first; never the secret or hash | 200 `ReporterKey[]` | 401, 403 |
| POST | `/admin/service-status/reporter-keys` | Create a key for a product; body `{ "productId": 3, "label": "production" }` (label optional); audited `SERVICE_REPORTER_KEY_CREATED` | 200 `CreatedReporterKey` (the **only** time the secret is returned) | 401, 403, 404 "Product not found" |
| DELETE | `/admin/service-status/reporter-keys/{id}` | Revoke a key (permanent; repeating it changes nothing); audited `SERVICE_REPORTER_KEY_REVOKED` | 200 `ReporterKey` | 401, 403, 404 "Reporter key not found" |

## Organization administrators
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/service-instances` | The caller's own organization's instances (no `DEPROVISIONED` ones), ordered by product then id | 200 `Instance[]` | 401, 403 (not an administrator of an organization in good standing) |

## Products (reporter key)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| POST | `/product-reports/health` | Report the health of instances, about every minute. Header `X-Reporter-Key: eisrk_…`; body `{ "reports": [HealthReport] }` with 1 to 200 reports | 200 `ReportResult` (partial success is a 200: see `rejected` and `errors`) | 400 "Send between 1 and 200 reports.", 401 "Invalid or revoked reporter key." (missing, unknown or revoked key) |

A report names its instance by `instanceId` or `subscriptionId`; the instance must belong to the product the key was created for.

## Request / response
```json
// POST /product-reports/health      X-Reporter-Key: eisrk_Qk3…
{ "reports": [
  { "instanceId": 17, "health": "DEGRADED", "message": "Queue is slow",
    "tenantReference": "tenant-9f2", "accessUrl": "https://valam.example/t/9f2" },
  { "subscriptionId": 88, "health": "HEALTHY" } ] }
// 200
{ "accepted": 1, "rejected": 1, "errors": ["Report 2: unknown instance for this product"] }
```
`HealthReport`: `instanceId` or `subscriptionId`, `health` (`HEALTHY`, `DEGRADED`, `DOWN`, case-insensitive; `UNKNOWN` is refused), optional `message` (300), `status` (`PROVISIONING`, `ACTIVE` or `FAILED`), `tenantReference` (100), `accessUrl` (500). Rejection messages: "unknown instance for this product", "health must be HEALTHY, DEGRADED or DOWN", "the instance is suspended" / "the instance is deprovisioned", "status must be PROVISIONING, ACTIVE or FAILED".

```json
// PUT /admin/service-status/instances/17
{ "status": "ACTIVE", "health": "DOWN", "note": "Product cannot report; confirmed down by phone" }
```
`ManualChange`: `status` and `health` both optional but at least one; `note` required (up to 300 characters kept).

```json
// 200 Instance
{ "id": 17, "subscriptionId": 88, "productId": 3, "productName": "Valam.ai",
  "ownerType": "ORGANIZATION", "ownerOrganizationId": 5, "ownerName": "Acme Ltd",
  "status": "ACTIVE", "health": "DEGRADED", "healthMessage": "Queue is slow",
  "lastReportedAt": "2027-02-10T08:15:02Z", "stale": false,
  "tenantReference": "tenant-9f2", "accessUrl": "https://valam.example/t/9f2",
  "changedBy": "PRODUCT", "changeNote": null, "createdAt": "2027-02-01T09:00:00Z" }
```
`ownerType` is `ORGANIZATION` or `INDIVIDUAL`; `stale` is true when there was no report in the last 5 minutes; `changedBy` is `SYSTEM`, `PRODUCT` or `ADMIN`; `changeNote` is the hand-change note or the subscription note ("Subscription suspended").

`Summary`: `{ "total": 12, "healthy": 9, "degraded": 1, "down": 0, "unknown": 0, "noRecentReport": 2 }` (`healthy`, `degraded`, `down` and `unknown` count only instances with a recent report).

`ReporterKey`: `id, productId, productName, label, keyPrefix, createdAt, lastUsedAt, revokedAt`. `CreatedReporterKey`: `{ "key": ReporterKey, "secret": "eisrk_…" }`.

## Status page fields
`GET /me/service-status` and `GET /admin/service-status` (see [service-status-page](../../02-requirements/FRD/service-status-page/api-requirements.md)) return each product as `ProductStatusDto`, which now also carries:

| Field | Meaning |
|---|---|
| `source` | `INSTANCES` when the status is derived from instances that reported in the last 5 minutes; `MANUAL` when an administrator set it, when it is Maintenance, or when no instance reported recently |
| `reportingInstances`, `downInstances`, `degradedInstances` | The instances behind an `INSTANCES` status; 0 for `MANUAL` |

Derivation: [BR-SRM-010](../../02-requirements/FRD/service-instances/business-rules.md). Frontend clients: `frontend/src/api/serviceInstancesApi.ts`, `serviceStatusApi.ts`.

OpenAPI contract: not maintained separately; the controllers (`AdminServiceInstanceController`, `OrganizationServiceInstanceController`, `ProductReportController`), `ServiceInstanceDtos` and this file are the source of truth ([C14](../../01-business/roadmap/open-decisions.md#c14)).

## Not built
Endpoints to start, stop, restart, scale, configure or delete a service; anomaly detection and alerts; usage metrics (all **Not specified**, [C101](../../01-business/roadmap/open-decisions.md#c101) SR-1 A).
