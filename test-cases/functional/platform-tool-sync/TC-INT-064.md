# TC-INT-064: Security pass: rate limit, tenant spoofing, no personal data in logs, no committed secrets, `azp` allow-lists

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-064 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
Both repositories; a real PostgreSQL for the spoofing test.

## Steps
1. Call the tool's platform tools more often than the limit; send messages with forged tenant references; log a database error that quotes an e-mail address; scan the added lines for secrets; review the allow-lists.

## Expected Result
Over the limit the platform is told `retry RATE_LIMITED` and the next minute starts fresh (0 switches it off); every forged tenant reference (schema names, `public`, SQL, paths, case, empty) is `UNKNOWN_TENANT`/`INVALID_PAYLOAD` and nothing is written; the log line carries only the exception class, root cause class and SQL state; no secret literal was found in the added lines. Review: [security review](../../../docs/08-architecture/security/platform-tool-sync-security-review.md).

## Automated coverage
- Macro: `PlatformCallerGuardTest`, `PlatformInboundPostgresTest.aTenantIsNeverChosen…`, `SafeLogTest`
- Platform: `ToolInboundTest` (allow-lists and guard)

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
