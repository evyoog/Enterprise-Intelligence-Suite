# Integrations

External systems the platform talks to — Keycloak (realm `eVyoog`), SAML identity providers, SMTP, and the hosted product suites (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai). One file per integration: purpose, protocol, auth, owner, failure handling.

- [platform-tool-sync-plan.md](platform-tool-sync-plan.md) — phase plan for synchronizing the platform with product tools (REQ-INT-003).
- [platform-tool-contract-v1.md](platform-tool-contract-v1.md) — the wire contract between the platform and tools.
- [payment-gateways.md](payment-gateways.md) — provider-independent payment gateways (REQ-BIL-003, C98, C99): security model, templates and administrator-added (configuration-only) gateways, how to add a provider or turn an added gateway into a real integration, webhook registration, what is not done.
- [razorpay.md](razorpay.md) — Razorpay, the one implemented payment provider.
