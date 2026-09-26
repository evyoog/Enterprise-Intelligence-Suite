# UI requirements — OIDC Identity-Provider Federation

| Screen | Where | Roles |
|---|---|---|
| OIDC section: list, add, edit, enable/disable, test, delete | `/organization/identity-federation`, below the SAML providers | Organization administrator (`MANAGE_ORGANIZATION`) |
| Organization SSO sign-in | Login dialog → "Sign in with your organization's SSO" → organization code | Anyone |

Fields: Name, Issuer (discovery) URL, Client ID, Client secret (password field; required on create, "leave blank to keep" on edit), Scopes (default `openid email profile`). Each provider shows its redirect URI to register at the identity provider. Test results list every check and error. Backend messages are shown as returned. Text under `oidc` in `en.json` and `es.json`; jest-axe checks in `OidcProvidersSection.test.tsx`.
