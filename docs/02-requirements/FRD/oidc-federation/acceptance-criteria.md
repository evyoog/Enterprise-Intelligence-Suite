# Acceptance criteria — OIDC Identity-Provider Federation

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** an organization administrator **When** they add an OIDC provider with a client secret **Then** it is listed with its redirect URI, the secret is stored encrypted and never returned, and editing with a blank secret keeps it | [TC-IAM-055](../../../../test-cases/functional/oidc-federation/TC-IAM-055.md) |
| AC-2 | **Given** a non-https issuer, scopes without openid, or no secret on create **When** the administrator saves **Then** it is refused with the backend message | [TC-IAM-056](../../../../test-cases/functional/oidc-federation/TC-IAM-056.md) |
| AC-3 | **Given** an enabled SAML provider **When** the administrator enables an OIDC provider (or the reverse) **Then** the other one is disabled and the SSO check reports the enabled protocol | [TC-IAM-057](../../../../test-cases/functional/oidc-federation/TC-IAM-057.md) |
| AC-4 | **Given** a provider **When** the administrator tests it **Then** discovery, issuer and required endpoints are each reported as a check or an error | [TC-IAM-058](../../../../test-cases/functional/oidc-federation/TC-IAM-058.md) |
| AC-5 | **Given** an enabled OIDC provider **When** a member enters the organization code and signs in at the provider **Then** they are provisioned as MEMBER and get a session; signing in again with the same subject maps to the same account | [TC-IAM-059](../../../../test-cases/functional/oidc-federation/TC-IAM-059.md) |
| AC-6 | **Given** a callback **When** the nonce, audience, issuer, signature or expiry is wrong, or there is no email **Then** sign-in is refused and the user sees the reason | [TC-IAM-060](../../../../test-cases/functional/oidc-federation/TC-IAM-060.md) |
| AC-7 | **Given** a sign-in state **When** it is reused, used for another organization, unknown, or the provider returns an error **Then** it is refused | [TC-IAM-061](../../../../test-cases/functional/oidc-federation/TC-IAM-061.md) |
| AC-8 | **Given** an organization that requires MFA, or a member whose membership was deactivated **When** they sign in through OIDC **Then** the sign-in goes to authenticator set-up, or is refused | [TC-IAM-062](../../../../test-cases/functional/oidc-federation/TC-IAM-062.md) |
| AC-9 | **Given** the OIDC section **When** it is shown **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, and jest-axe reports no violations | [TC-IAM-063](../../../../test-cases/functional/oidc-federation/TC-IAM-063.md) |
