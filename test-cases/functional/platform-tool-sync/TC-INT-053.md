# TC-INT-053: Only tools with a ready tenant and a live subscription hear about a change; a paused tool keeps its messages

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-053 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-1, AC-2](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Three organizations: SUSPENDED, CANCELLED and no subscription; one tool ACTIVE, then PAUSED. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Change the city of each organization.
2. Deliver.
3. Pause the tool, change an organization, deliver, resume, deliver.

## Expected Result
Only the organization with an ACTIVE or SUSPENDED subscription and a READY tenant is sent; the others get nothing. While paused nothing is sent and the change is kept as a waiting message; after Resume it is sent. A person without a Keycloak id, a membership of such a person, an individual's subscription and access to a product without a tool produce no message.

## Automated coverage
- `.../toolsync/service/ToolDeliveryTest.aSuspendedSubscriptionStillGetsDataButACancelledOrMissingOneDoesNot`, `.aPausedToolKeepsItsMessagesPendingAndNothingIsLost`
- `.../toolsync/service/ToolSyncPublishingTest` (`aPersonWithoutAKeycloakIdOrWithoutAMembershipIsNotPublished`, `anOrganizationSubscriptionIsPublishedAndAnIndividualsIsNot`, `accessToAProductThatHasNoToolIsNotPublished`, `aPausedToolStillGetsEventsSoNothingIsLostWhileItIsPaused`)

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
