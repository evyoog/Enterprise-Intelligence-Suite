# Business rules — Claim Mapping

Decided in [C23](../../../01-business/roadmap/open-decisions.md#c23) and [C28](../../../01-business/roadmap/open-decisions.md#c28). Enforced by the backend.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-007.1 | Each SAML and OIDC provider can name the attribute / claim for email, first name, last name and display name. Each name is optional (max 255); blank means "use the defaults". Management needs `MANAGE_ORGANIZATION` on the caller's own organization (403 for another organization's provider, 404 for an unknown id). | backend | `ClaimMapping`, `SamlProviderService#updateClaimMapping`, `OidcProviderService#updateClaimMapping` |
| BR-IAM-007.2 | A configured name is tried first, then the defaults, in order of preference. SAML defaults: email `email, emailaddress, mail, …/claims/emailaddress, urn:oid:0.9.2342.19200300.100.1.3`; first name `firstname, givenname, …`; last name `lastname, surname, sn, …`; display name `displayname, name, cn`. OIDC defaults: `email`, `given_name`, `family_name`, `name`. SAML attribute names match case-insensitively. | backend | `ClaimMapping#tryFirst`, `SamlAuthenticationService#firstAttribute`, `OidcAuthenticationService#firstClaim` |
| BR-IAM-007.3 | The fallbacks stay fixed (not configurable): SAML email falls back to an email-style NameID; first name falls back to display name, then the email's local part; last name falls back to "SSO User". | backend | `SamlAuthenticationService#handleAcs`, `OidcAuthenticationService#handleCallback` |
| BR-IAM-007.4 | With no mapping set, sign-in behaves exactly as before this feature. | backend | `ClaimMapping#orEmpty` (null columns) |
| BR-IAM-007.5 | Role mapping is out of scope: federated users join as `MEMBER` whatever the claims say. | backend | `FederatedAccountService#ensureActiveMembership` |
| BR-IAM-007.6 | Changes are audited: `SAML_CLAIM_MAPPING_UPDATED`, `OIDC_CLAIM_MAPPING_UPDATED`. | backend | provider services |
