# API requirements — OIDC Identity-Provider Federation

Paths are relative to `/api`. Management endpoints need `MANAGE_ORGANIZATION` on the caller's own organization.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/oidc-providers` | List | 200 `OidcProviderDto[]` | 401, 403, 404 |
| POST | `/organization/me/oidc-providers` | Create | 201 `OidcProviderDto` | 400, 401, 403 |
| PUT | `/organization/me/oidc-providers/{id}` | Update (blank secret keeps it) | 200 `OidcProviderDto` | 400, 401, 403, 404 |
| POST | `/organization/me/oidc-providers/{id}/enable` · `/disable` | Enable / disable | 200 `OidcProviderDto` | 401, 403, 404 |
| POST | `/organization/me/oidc-providers/{id}/test` | Test discovery | 200 `OidcProviderTestResultDto` | 401, 403, 404 |
| DELETE | `/organization/me/oidc-providers/{id}` | Delete | 204 | 401, 403, 404 |
| GET | `/saml/sso-check?organizationCode=` | Public: is SSO available, and which `protocol` (`SAML` / `OIDC`) | 200 `SsoCheckResponseDto` | - |
| GET | `/oidc/{organizationId}/login-init` | Public: redirect to the provider | 302 | 302 to `/?ssoError=` |
| GET | `/oidc/{organizationId}/callback?code&state[&error]` | Public: finish sign-in | 302 to the app (or `/?mfaEnroll=` / `/?mfaChallenge=`) | 302 to `/?ssoError=` |

Request body (`OidcProviderRequest`): `{ "name", "issuerUrl", "clientId", "clientSecret", "scopes" }`.
`OidcProviderDto`: `id, name, issuerUrl, clientId, clientSecretSet, scopes, enabled, redirectUri, createdAt, updatedAt`. `redirectUri` = `{backend-url}/oidc/{organizationId}/callback`, to register at the provider.

Frontend client: `oidcApi` (`frontend/src/api/oidcApi.ts`); sign-in start in `samlLoginApi.loginInitUrl(id, protocol)`. Tables: `oidc_identity_provider`, `oidc_login_request`, `oidc_external_identity` (migration V004).
