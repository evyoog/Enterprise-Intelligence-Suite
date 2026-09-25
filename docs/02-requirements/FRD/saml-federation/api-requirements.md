# API requirements — SAML Federation — Edit Provider

All endpoints **already exist** on branch `dev`. Paths are relative to the backend base path `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/organization/me/saml-providers` | List providers (existing) | `MANAGE_ORGANIZATION` | 200 `SamlProviderDto[]` | 403, 404 |
| PUT | `/organization/me/saml-providers/{id}` | Edit a provider | `MANAGE_ORGANIZATION` | 200 `SamlProviderDto` | 400, 403, 404 |

## Request / response
`UpdateSamlProviderRequest`:
```json
{ "name": "…", "metadataXml": "…", "entityId": "…", "ssoUrl": "…", "certificatePem": "…" }
```
All fields are optional (see business rules 3–6).

`SamlProviderDto`: `id`, `name`, `entityId`, `ssoUrl`, `certificatePem`, `certificateFingerprint`, `certificateExpiresAt`, `certificateExpired`, `enabled`, `createdAt`, `updatedAt`.

Frontend client: `samlApi.update` already exists in `frontend/src/api/samlApi.ts`.

OpenAPI contract: `docs/06-api/openapi/API-<APP-CODE>-<NNN>.yaml` (not created). The running backend serves its live spec at `/api/swagger-ui.html` in dev and uat.
