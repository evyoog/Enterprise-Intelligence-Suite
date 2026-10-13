# Business rules — API management (REQ-INT-001)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | The full key is returned only in the create response. Only its prefix and SHA-256 hash are stored; it can never be shown again. | backend | .1 |
| BR-2 | A key is usable only while ACTIVE: not revoked and not past its expiry. A revoked or expired key gets 401 and the use of a revoked key is audited. | backend | .1, .5 |
| BR-3 | A key acts as the organization's oldest active administrator who can sign in (permissions read live); platform administration is excluded. When no such administrator exists the key is refused (401) and `API_KEY_NO_ADMINISTRATOR` is audited. Legacy personal keys act as their owner. | backend | .1 |
| BR-4 | An organization administrator lists and revokes only their own organization's keys (another organization's key: 404). Revocation is permanent. A key cannot create or revoke keys. | backend | .1 |
| BR-5 | At most 10 active keys per organization (default). Expiry, when given, must be in the future. Name 1–100 characters. Scope must be `READ` or `READ_WRITE`. | backend | .1, .7 |
| BR-6 | Every request counts against one bucket: the API key if present, else the signed-in user, else the client IP. Over the limit: 429, `Retry-After` = seconds until the window resets, and the request is not processed. | backend | .2 |
| BR-7 | `/v1/<path>` and `/<path>` reach the same endpoint with the same security rules; every response carries `API-Version: 1`. | backend | .3 |
| BR-8 | Each successful key authentication updates the key's last-used time and request count. | backend | .4 |
| BR-9 | A `READ` key may use only `GET`, `HEAD` and `OPTIONS`; any other method gets 403 `API_KEY_READ_ONLY` before the endpoint runs. A key without a scope is `READ`; a legacy personal key is `READ_WRITE`. (C92, API-4 B) | backend | .7 |
| BR-10 | A request whose path starts with a configured deprecated prefix carries `Deprecation: true`, `Sunset: <date>` and, if configured, `Link: <successor>; rel="successor-version"`; the request is otherwise unchanged. The prefix is matched on path segments (`/api/foo` does not match `/api/foobar`). (C92, API-5 B) | backend | .6 |
