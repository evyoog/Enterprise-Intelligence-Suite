# API requirements — API management (module `integration`)

Requirement: [REQ-INT-001](../../02-requirements/FRD/api-management/requirement.md).

## Authentication with a key
Send `X-API-Key: eis_ab12cd34_…` (and no `Authorization` header). An invalid, revoked or expired key gets **401** with the standard error body. A key acts as an administrator of its organization, except platform administration (`/admin/**` → 403). A **read-only** key may only use `GET`, `HEAD` and `OPTIONS`; any other method gets **403** with code `API_KEY_READ_ONLY`. If the organization has no active administrator who can sign in, the key gets **401** (`API_KEY_NO_ADMINISTRATOR` is audited).

## Versioning
Every endpoint is served at `/api/v1/<path>` as well as `/api/<path>`; responses carry `API-Version: 1`.

## Deprecation
Under a path prefix listed in `app.api.deprecations` every response carries `Deprecation: true`, `Sunset: <HTTP date>` and, when a successor is set, `Link: <successor>; rel="successor-version"`. Empty until `/v2` exists. Example: `app.api.deprecations[0].path-prefix=/api/v1/orders`, `.sunset=2027-06-30`, `.successor=/api/v2/orders`.

## Rate limits
Responses carry `X-RateLimit-Limit` and `X-RateLimit-Remaining`. Over the limit: **429**, `Retry-After: <seconds>`, body `{ "status": 429, "error": "Too Many Requests", "code": "RATE_LIMITED", "message": "…" }`.

## Endpoints
`ApiKey` fields: `id, name, prefix, status, scope (READ | READ_WRITE), personal (true = legacy personal key), createdByName, createdAt, expiresAt, lastUsedAt, requestCount` and, in the create response only, `key`.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/api-keys` | The organization's keys (no secret); permission *Manage organization* | 200 | 401, 403 |
| POST | `/organization/me/api-keys` | Create `{ name, scope, expiresAt? }`; the response's `key` holds the full key **once** | 201 | 400 (name, scope, expiry in the past, more than 10 active), 401, 403 (not an administrator, or the caller is itself a key) |
| POST | `/organization/me/api-keys/{id}/revoke` | Revoke one of the organization's keys | 200 | 403, 404 (another organization's key), 409 (already revoked) |
| GET | `/me/api-keys` | The caller's **legacy personal** keys only | 200 | 401 |
| POST | `/me/api-keys` | **No longer creates keys** | 403 "API keys belong to your organization…" | |
| POST | `/me/api-keys/{id}/revoke` | Revoke one of the caller's legacy keys | 200 | 404, 409 |
| GET | `/admin/api-keys?page=` | Every key with owner, organization, scope, last used and request count (`MANAGE_INTEGRATIONS`) | 200 | 403 |

```json
// POST /organization/me/api-keys  { "name": "CRM sync", "scope": "READ" } → 201
{ "id": 3, "name": "CRM sync", "prefix": "eis_ab12cd34", "status": "ACTIVE", "scope": "READ", "personal": false,
  "createdByName": "Asha Rao", "createdAt": "2026-10-13T09:00:00Z", "expiresAt": null, "lastUsedAt": null, "requestCount": 0,
  "key": "eis_ab12cd34_Q8…(40 characters)" }
```
