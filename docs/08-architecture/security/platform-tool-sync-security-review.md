# Security review — platform ↔ tool synchronization (phase 8)

[REQ-INT-003](../../02-requirements/FRD/platform-tool-sync/requirement.md). Reviewed 2026-10-10 on the code of both repositories as of phase 8. Result: no open high finding; the items under "Open" are known and scheduled.

## What was checked and how

| Area | Check | Result |
|---|---|---|
| Secrets in the change | Pattern scan of every line added since phase 1 (Macro) and since the phase 6 plan commit (EIS) for credential assignments with literal values | Nothing found. The platform's `SYNC_CLIENT_SECRET` and the tool's `PLATFORM_SYNC_CLIENT_SECRET` have **empty defaults**; the Keycloak guide says they come from the secrets manager. The only literals in tests are fixtures for fake servers |
| `azp` allow-lists | Platform: a call is allowed only if the token's `azp` is the `client_id` of an ACTIVE `tool_connector`, the organization exists, the tool holds an ACTIVE subscription of it, and (for changes) its tenant is READY; a paused tool is refused; a tool can touch only its own organizations and nodes. Tool: `platform.inbound.allowed-clients` (default `eis-sync`) is separate from the Agile Planner's `app.mcp.allowed-clients` | Covered by `ToolInboundTest.onlyAnActiveToolWith…`, `aToolCannotTouchANodeOfAnotherOrganization`, `PlatformCallerGuardTest` |
| Token audience | Both sides accept a token only if their own client id is in `aud` or is the `azp` (the platform's `ClientAudienceValidator`; the Macro's entitlement filter for people). A service client therefore needs an Audience mapper | Documented in the Keycloak guide; proven by the end-to-end run (tokens with the mapper work) |
| Rate limits | Platform: the existing per-user limit (300 a minute, `app.rate-limit.per-user`) applies to each service account on `/api/mcp` (by reading the filter; not tested separately). Tool: **new** per-client limit `platform.inbound.rate-limit-per-minute` (default 1200); over it the platform is told `retry RATE_LIMITED` and backs off | `PlatformCallerGuardTest.aClientOverItsRateLimit…` |
| Tenant spoofing | A message chooses its tenant only through the platform organization id in the registry. Schema names, the control schema, `public`, quoted SQL, paths, case and empty values are all `UNKNOWN_TENANT`/`INVALID_PAYLOAD` and write nothing; `provision_tenant` never takes a database or schema name; a reconcile call is checked the same way | `PlatformInboundPostgresTest.aTenantIsNeverChosen…` (real PostgreSQL) |
| Logs and personal data | Every log line of both synchronization modules was read. Payloads, e-mail addresses and names are not logged; only ids (a Keycloak id, an organization id, a delivery id), tool names and reasons. **Found and fixed:** a database error message can quote the data being written (a duplicate-key error names the e-mail), and several places logged `exception.toString()`. They now log the exception class, the root cause class and the SQL state only (`SafeLog` on the tool, the same rule on the platform); the provisioning failure summary does the same for database errors | `SafeLogTest` |
| Single writer and idempotency | Tools never write to the platform except through the contract; the first result of a call is stored by a hash of (client id, idempotency key), so one tool cannot read another's results | `ToolInboundTest` |
| Entitlement | The tool's entitlement filter is fail-closed for sensitive actions; the platform's `get_entitlement` answer is authoritative | End-to-end `s6`, `s7` |
| Data that is never sent | Passwords, tokens, MFA secrets, payment and bank data: none of those fields exist in the snapshot builders | `ToolMessageBuilder` (reviewed) |

## Open

1. **Committed development defaults** in the platform's `application.yml` (impersonation secret, shared SSO secret, a sandbox database account) and in the Macro's `application.yml` (a sandbox database password). They are not part of this synchronization and were found before it. **Rotate any value that was ever used outside a developer machine**, then remove the defaults.
2. **`INTERNAL_SSO_SHARED_SECRET`** is one secret shared by the apps for the sign-in bridge's backend calls. [Retirement plan](../../09-integrations/sso-shared-secret-retirement.md).
3. The platform's rate limit is per instance (in memory), like its other limits; with several instances the effective limit is multiplied.
4. A person's profile is global (one customer in many organizations), so `update_user_profile` from one tool changes it for every organization the person belongs to; the other tools receive the change.
