# Acceptance criteria — API management (REQ-INT-001)

| ID | Requirement | Criterion | Test case |
|---|---|---|---|
| AC-1 | .1 | **Given** a signed-in user **when** they create a key named "CRM sync" **then** the response contains the full key once; the list shows only its name, prefix, created date and ACTIVE status; the database holds no full key (full key shown once). | [TC-INT-001](../../../../test-cases/functional/api-management/TC-INT-001.md) |
| AC-2 | .1 | **Given** an active key **when** it calls `/me/...` with `X-API-Key` **then** the call succeeds as the key's owner. | [TC-INT-002](../../../../test-cases/functional/api-management/TC-INT-002.md) |
| AC-3 | .1, .5 | **Given** a revoked key **when** it is used **then** 401 and an audit entry for the attempt (revoked key rejected); an expired key also gets 401. | [TC-INT-003](../../../../test-cases/functional/api-management/TC-INT-003.md) |
| AC-4 | .1 | **Given** user B **when** B revokes user A's key **then** 404 and A's key stays ACTIVE. | [TC-INT-003](../../../../test-cases/functional/api-management/TC-INT-003.md) |
| AC-5 | .2 | **Given** a limit of N per minute **when** request N+1 arrives in the same minute **then** 429 with a `Retry-After` header (429 with Retry-After). | [TC-INT-004](../../../../test-cases/functional/api-management/TC-INT-004.md) |
| AC-6 | .3 | **Given** any endpoint **when** called at `/v1/<path>` **then** the result equals `/<path>`, security rules are the same, and the response has `API-Version: 1`. | [TC-INT-005](../../../../test-cases/functional/api-management/TC-INT-005.md) |
| AC-7 | .4 | **Given** keys that were used **when** a platform administrator opens API key usage **then** each key shows owner, last-used time and request count; without `MANAGE_INTEGRATIONS`: 403. | [TC-INT-006](../../../../test-cases/functional/api-management/TC-INT-006.md) |
| AC-8 | .5 | **Given** a key is created or revoked **then** an audit entry names the owner and the key prefix. | [TC-INT-001](../../../../test-cases/functional/api-management/TC-INT-001.md) |
| AC-9 | Accessibility | **Given** the API keys section, the create dialog (with the one-time key) and the revoke confirmation **then** each passes the axe test. | [TC-INT-007](../../../../test-cases/functional/api-management/TC-INT-007.md) |
