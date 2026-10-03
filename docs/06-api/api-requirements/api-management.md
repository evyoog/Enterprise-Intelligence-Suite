# API requirements — API management (module `integration`)

Requirement: [REQ-INT-001](../../02-requirements/FRD/api-management/requirement.md).

## Authentication with a key
Send `X-API-Key: eis_ab12cd34_…` (and no `Authorization` header). An invalid, revoked or expired key gets **401** with the standard error body. A key reaches the same endpoints as its owner, except platform administration (`/admin/**` → 403).

## Versioning
Every endpoint is served at `/api/v1/<path>` as well as `/api/<path>`; responses carry `API-Version: 1`.

## Rate limits
Responses carry `X-RateLimit-Limit` and `X-RateLimit-Remaining`. Over the limit: **429**, `Retry-After: <seconds>`, body `{ "status": 429, "error": "Too Many Requests", "code": "RATE_LIMITED", "message": "…" }`.

## Endpoints
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/me/api-keys` | The caller's keys (no secret) | 200 | 401 |
| POST | `/me/api-keys` | Create `{ name, expiresAt? }`; the response's `key` holds the full key **once** | 201 | 400 (name, expiry in the past, more than 10 active), 401, 404 (no Vyoog account) |
| POST | `/me/api-keys/{id}/revoke` | Revoke one of the caller's keys | 200 | 404 (not the caller's), 409 (already revoked) |
| GET | `/admin/api-keys?page=` | Every key with owner, last used and request count (`MANAGE_INTEGRATIONS`) | 200 | 403 |

```json
// POST /me/api-keys → 201
{ "id": 3, "name": "CRM sync", "prefix": "eis_ab12cd34", "status": "ACTIVE",
  "createdAt": "2026-10-03T09:00:00Z", "expiresAt": null, "lastUsedAt": null, "requestCount": 0,
  "key": "eis_ab12cd34_Q8…(40 characters)" }
```
