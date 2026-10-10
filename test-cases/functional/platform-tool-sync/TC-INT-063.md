# TC-INT-063: Metrics show lag, failures, drift and refusals on both sides

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-063 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P2 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
The platform's meter registry; the Macro's `PlatformSyncMetrics`.

## Steps
1. Deliver messages, let one fail until FAILED, find drift with reconcile, ask for an entitlement of a stranger.
2. Read `platformsync.*` on both sides.

## Expected Result
`platformsync.delivery{tool,status}`, `.delivery.failed{tool}`, `.lag.seconds{tool}` (a summary), the gauges for waiting, failed and the oldest waiting message, `.drift.detected{tool,type}` and `.entitlement.denied{reason}` exist and count what happened; on the tool `platformsync.lag.seconds{tool}` and `.drift.detected{type}` in addition to the earlier inbound/outbound/entitlement/provisioning ones; nothing is recorded when metrics are off. In the end-to-end story no delivery failed. Thresholds: [operations](../../../docs/09-integrations/platform-tool-operations.md).

## Automated coverage
- `.../toolsync/service/ToolSyncMetricsTest` (3 tests)
- Macro: `PlatformSyncMetricsTest` (3 tests)
- `backend/src/test/java/com/vyoog/eisplatform/modules/toolsync/service/e2e/PlatformMacroEndToEndTest.java` `s10_theMetricsShowTheLagAndNothingFailed`

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
