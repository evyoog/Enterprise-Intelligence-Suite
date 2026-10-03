# Business rules — API management (REQ-INT-001)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | The full key is returned only in the create response. Only its prefix and SHA-256 hash are stored; it can never be shown again. | backend | .1 |
| BR-2 | A key is usable only while ACTIVE: not revoked and not past its expiry. A revoked or expired key gets 401 and the use of a revoked key is audited. | backend | .1, .5 |
| BR-3 | A key acts with its owner's identity and permissions (organization permissions read live); platform administration is excluded (C61 default). | backend | .1 |
| BR-4 | A user lists and revokes only their own keys (another user's key: 404). Revocation is permanent. | backend | .1 |
| BR-5 | At most 10 active keys per owner (default). Expiry, when given, must be in the future. Name 1–100 characters. | backend | .1 |
| BR-6 | Every request counts against one bucket: the API key if present, else the signed-in user, else the client IP. Over the limit: 429, `Retry-After` = seconds until the window resets, and the request is not processed. | backend | .2 |
| BR-7 | `/v1/<path>` and `/<path>` reach the same endpoint with the same security rules; every response carries `API-Version: 1`. | backend | .3 |
| BR-8 | Each successful key authentication updates the key's last-used time and request count. | backend | .4 |
