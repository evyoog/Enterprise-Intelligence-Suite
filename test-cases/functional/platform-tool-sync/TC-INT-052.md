# TC-INT-052: A change is one message per tool and organization, built from the current state, with the aggregate's version

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-052 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-1, AC-2](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A connected tool (`tool_connector` ACTIVE), an organization with a READY tenant and an ACTIVE subscription; a fake tool (the test gateway). The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Rename the organization, then change its city, before the dispatcher runs.
2. Run the outbox dispatcher and the delivery run.
3. Read the calls the tool received.

## Expected Result
The tool receives exactly one `upsert_organization` with `contractVersion` 1, `tenantRef` = the organization id, `aggregateType` Organization, `version` = the organization's current `sync_version`, an `occurredAt` ending `+05:30`, and the latest name and city (two changes before delivery are one message). Real MCP: the envelope goes as the `envelope` argument with a bearer token fetched with client credentials, the token is reused, and the tool's result object is read back (`McpToolGatewayTest`).

## Automated coverage
- `.../toolsync/ToolDeliveryTest.anOrganizationChangeReachesTheToolWithTheCurrentStateAndItsVersion`
- `.../toolsync/McpToolGatewayTest` (5 tests: envelope argument and bearer, token reuse, retry answer, unreachable tool or refusing identity provider, no configuration)
- `.../toolsync/ToolSyncPublishingTest` (18 tests: versions per aggregate, events per change, one event per aggregate per transaction, nothing on rollback, nothing without a tool)

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
