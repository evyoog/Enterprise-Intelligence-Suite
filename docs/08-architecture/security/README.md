# Security architecture

Authentication (Keycloak SSO, platform TOTP MFA, SAML federation), authorization (RBAC permissions, privileged access), secrets handling, and threat model.

## Documents
- [platform-admin-registry.md](platform-admin-registry.md): the JSON file that lists platform administrator accounts (identities only, never passwords), what it grants and does not, configuration, and how to move it to S3 ([C108](../../01-business/roadmap/open-decisions.md#c108)).
