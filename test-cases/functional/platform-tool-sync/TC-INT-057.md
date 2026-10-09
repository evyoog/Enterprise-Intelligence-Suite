# TC-INT-057: A tool changes profiles and the hierarchy under the platform's own rules, with versions and stale-version protection

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-057 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-4, AC-5](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A connected organization with a root node and one member. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Create a division and a department; create a sibling with the same name; move the division under its own department; delete the division; rename the department with its current and an older `expectedVersion`; delete the department.
2. Update a member's name with the current and an older `expectedVersion`; update a stranger.

## Expected Result
The rules are the hierarchy service's: duplicate sibling names are rejected, a move under a descendant is `CYCLE`, deleting a node with children is `NODE_IN_USE`, an older `expectedVersion` is `STALE_VERSION` with the current snapshot and nothing changes, a node of another organization cannot be touched. A delete answers the tombstone version (the node's version plus one) with an `OrgNodeDeleted` snapshot. The audit entry names the tool as the actor (no person). Every change also becomes the usual events and reaches the other tools. A profile change bumps the person's version; a stranger is `NOT_A_MEMBER`.

## Automated coverage
- `.../toolsync/ToolInboundTest.aToolCreatesMovesAndDeletesNodesUnderThePlatformsOwnRules`, `.aToolCannotTouchANodeOfAnotherOrganization`, `.aProfileChangeBumpsTheVersionAndAStaleVersionIsRejectedWithTheCurrentSnapshot`
- `.../toolsync/ToolSyncPublishingTest.aStaleCopySavedAgainNeverMakesTheVersionGoBack`

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
