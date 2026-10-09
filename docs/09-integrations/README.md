# Integrations

External systems the platform talks to — Keycloak (realm `eVyoog`), SAML identity providers, SMTP, and the hosted product suites (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai). One file per integration: purpose, protocol, auth, owner, failure handling.

- [platform-tool-sync-plan.md](platform-tool-sync-plan.md) — phase plan for synchronizing the platform with product tools (REQ-INT-003).
- [platform-tool-contract-v1.md](platform-tool-contract-v1.md) — the wire contract between the platform and tools.
- [../../deployment/keycloak-sync-clients.md](../../deployment/keycloak-sync-clients.md) — the Keycloak service clients (`eis-sync`, `<tool>-sync`), their audience mapper and the platform's `SYNC_*` settings.
- [../06-api/api-requirements/platform-tool-sync.md](../06-api/api-requirements/platform-tool-sync.md) and [../07-database/data-model/platform-tool-sync.md](../07-database/data-model/platform-tool-sync.md) — the platform side as built (phase 7): MCP tools, admin monitor, tables.
