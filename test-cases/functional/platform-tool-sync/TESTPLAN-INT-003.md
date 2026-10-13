# Test plan — REQ-INT-003 Platform ↔ tool synchronization

Status: planned (FRD Approved 2026-10-09; phase 1 built in the Macro repository, cases TC-INT-018–021 there). Individual test cases `TC-INT-018` onward are written with the phase that builds each behaviour, in the repository where it is built; this plan fixes the scope and numbering.

Acceptance source: [acceptance criteria](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md). Contract: [contract v1](../../../docs/09-integrations/platform-tool-contract-v1.md). Build order: [phase plan](../../../docs/09-integrations/platform-tool-sync-plan.md).

| Reserved ids | Covers | Phase | Repo | Automated |
|---|---|---|---|---|
| TC-INT-018 – 021 | Tenant registry, routing, refusal of unknown or suspended tenant, no schema chosen by header (AC-13) | 1 | Macro | Yes |
| TC-INT-022 – 029 | Inbound apply: idempotency, out-of-order versions, hierarchy (children first, cycle, in-use delete), user upsert without business records, wrong client, wrong tenant (AC-3, AC-11) | 2 | Macro | Yes (platform simulator) |
| TC-INT-030 – 034 | Write-through, tool-created user (reuse, create, compensation), platform unreachable (AC-4, AC-5, AC-6) | 3 | Macro | Yes |
| TC-INT-035 – 038 | SSO bridge: redeem, refresh, logout both ways, wrong secret, stale cookie | 4 | Macro | Partly (two-app browser flow is manual) |
| TC-INT-039 – 046 | Entitlement matrix, subscription end at 23:59:00.000 +05:30, revocation within 5 minutes, sensitive action fail-closed, first-login closed (AC-7–AC-10) | 5 | Macro | Yes |
| TC-INT-047 – 050 | Provisioning idempotency, failure and resume, adoption dry run, no deletion on expiry (AC-12) | 6 | Macro | Yes |
| TC-INT-051 – 058 | Event publishing, versions, fan-out only to subscribed and ready tools, per-tool failure isolation, retry then FAILED, replay, MCP server guard, end-time storage and migration (AC-1, AC-2, AC-8) | 7 | EIS | Yes |
| TC-INT-059 – 064 | End-to-end scenarios on a staging pair, reconcile repair, metrics, security pass (AC-14–AC-16) | 8 | Both | Partly |
| TC-INT-065 + | Per-tool onboarding checklist runs | 9 | Each tool | Yes |
