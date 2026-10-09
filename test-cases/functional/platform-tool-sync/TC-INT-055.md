# TC-INT-055: The Tool sync tab shows tools, tenants and messages; its actions are permitted only to MANAGE_INTEGRATIONS and are audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-055 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-2, AC-15](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator and a plain user; a tool with a READY tenant, waiting, failed and delivered messages. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Open Platform events → Tool sync as each user.
2. Use Retry, Replay, Pause, Resume, Start and Reconcile.
3. Reconcile an organization whose tool lost one hierarchy node, and one whose tool is stopped.

## Expected Result
Anonymous gets 401 and a plain user 403 on every `/admin/events/tool-sync/**` call. The administrator sees each tool, each organization's tenant status, schema version, last delivered, waiting and failed counts and last error, and every message with attempts and error. Each action writes an audit entry with the administrator (`TOOL_DELIVERY_RETRIED`, `TOOL_DELIVERY_REPLAYED`, `TOOL_CONNECTOR_PAUSED/RESUMED`, `TOOL_TENANT_STARTED`, `TOOL_TENANT_RECONCILED`). Start asks for provisioning when there is no tenant (400 without an ACTIVE subscription) and resyncs otherwise. Reconcile finds only the lost node, sends only it, and a second reconcile reports in sync; an unreachable tool is reported and nothing is changed.

## Automated coverage
- `.../toolsync/AdminToolSyncTest` (5 tests)
- `.../toolsync/ToolDeliveryTest.reconcileFindsWhatTheToolLostAndResendsOnlyThat`, `.reconcileSaysSoWhenTheToolCannotBeReachedAndChangesNothing`
- `frontend/src/pages/admin/ToolSyncPanel.test.tsx` (9 tests incl. a11y) and `AdminPlatformEventsPage.test.tsx` (tab)

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
