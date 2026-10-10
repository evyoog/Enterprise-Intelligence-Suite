# Integrations

External systems the platform talks to — Keycloak (realm `eVyoog`), SAML identity providers, SMTP, and the hosted product suites (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai). One file per integration: purpose, protocol, auth, owner, failure handling.

- [platform-tool-sync-plan.md](platform-tool-sync-plan.md) — phase plan for synchronizing the platform with product tools (REQ-INT-003).
- [platform-tool-contract-v1.md](platform-tool-contract-v1.md) — the wire contract between the platform and tools.
- [../../deployment/keycloak-sync-clients.md](../../deployment/keycloak-sync-clients.md) — the Keycloak service clients (`eis-sync`, `<tool>-sync`), their audience mapper and the platform's `SYNC_*` settings.
- [../06-api/api-requirements/platform-tool-sync.md](../06-api/api-requirements/platform-tool-sync.md) and [../07-database/data-model/platform-tool-sync.md](../07-database/data-model/platform-tool-sync.md) — the platform side as built (phase 7): MCP tools, admin monitor, tables.
- [platform-tool-operations.md](platform-tool-operations.md) — metrics, alert thresholds, runbook and the end-to-end test (phase 8).
- [tool-onboarding-checklist.md](tool-onboarding-checklist.md) — what a product must do to become a tool.
- [sso-shared-secret-retirement.md](sso-shared-secret-retirement.md) — plan to replace `INTERNAL_SSO_SHARED_SECRET` with per-app service clients.
- [../08-architecture/security/platform-tool-sync-security-review.md](../08-architecture/security/platform-tool-sync-security-review.md) — the phase 8 security review.
