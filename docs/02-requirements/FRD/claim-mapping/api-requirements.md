# API requirements — Claim Mapping

The API shape was left open in this FRD; under C14 the implemented endpoints are the source of truth. A dedicated endpoint per provider type keeps the existing create / update request bodies unchanged.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| PUT | `/organization/me/saml-providers/{id}/claim-mapping` | Replace a SAML provider's mapping | `MANAGE_ORGANIZATION` | 200 `SamlProviderDto` | 400, 401, 403, 404 |
| PUT | `/organization/me/oidc-providers/{id}/claim-mapping` | Replace an OIDC provider's mapping | `MANAGE_ORGANIZATION` | 200 `OidcProviderDto` | 400, 401, 403, 404 |

Body (`ClaimMappingDto`): `{ "email": "upn", "firstName": null, "lastName": null, "displayName": "displayName" }`. Both provider DTOs return the same object as `claimMapping`. Columns `email_claim`, `first_name_claim`, `last_name_claim`, `display_name_claim` on both provider tables (migration V005).

Frontend: `samlApi.updateClaimMapping`, `oidcApi.updateClaimMapping`, `ClaimMappingDialog`.
