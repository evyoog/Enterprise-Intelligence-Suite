# TC-INT-056: The platform's MCP server accepts only active tools, for organizations they are subscribed to, and does each call once

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-056 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-3, AC-11](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A tool connector with client `thittam-sync`; organizations with and without a subscription and a ready tenant. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Call `report_user_created` with no client, an unknown client, an unknown organization, an organization without an active subscription, one whose tenant is not ready, and from a paused tool.
2. Call it correctly twice with the same idempotency key.
3. Call `/api/mcp` without a token.

## Expected Result
The refusals are results (not protocol errors): `NOT_ALLOWED_CLIENT`, `UNKNOWN_TENANT`, `NO_ACTIVE_SUBSCRIPTION_FOR_TENANT`, `TENANT_NOT_READY`. The correct call creates the customer (e-mail lower-cased) and an ACTIVE `MEMBER` membership with **no product access**, answers `data.snapshots` User and Membership with versions, and the repeat returns the first result without creating anything. An existing person is never merged by an unverified e-mail (`EMAIL_NOT_VERIFIED`), a verified one is linked by `sub`, another person's e-mail is `INVALID_PAYLOAD`, a full organization is `SEAT_LIMIT_EXCEEDED`, and a rejected call changes nothing. Without a token the endpoint answers 401. The eight tools are listed over real MCP and results come back as results.

## Automated coverage
- `.../toolsync/ToolInboundTest` (`onlyAnActiveToolWithAnActiveSubscriptionAndAReadyTenantMayCall`, `aPersonRegisteredInTheToolBecomesACustomerAndAMemberWithNoProductAccessAndARepeatDoesNothingMore`, `aPersonIsNeverMergedByAnUnverifiedEmailButIsLinkedWhenTheEmailIsVerified`, `aFullOrganizationRefusesANewMemberAndAnInvalidCallChangesNothing`)
- `.../toolsync/PlatformMcpProtocolTest` (4 tests, embedded Tomcat) and `PlatformMcpSecurityTest` (2 tests)

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
