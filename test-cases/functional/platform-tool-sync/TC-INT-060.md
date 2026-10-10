# TC-INT-060: Hierarchy changes and people with access appear in the real Macro with the same structure, role and node

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-060 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-1, AC-3](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
TC-INT-059 done (the same story continues).

## Steps
1. Add a node under Press Shop; add a second division and move the new node under it.
2. Add a person with `PMS_MANAGER` access placed on Press Shop.

## Expected Result
The new node appears in the Macro under the same parent (by platform id); after the move it is under the new parent, so the structure is the same on both sides. The person is a user in the Macro, active, with role `PROJECT_OWNER` and placed on the Macro's Press Shop node.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/toolsync/service/e2e/PlatformMacroEndToEndTest.java` `s2_aNodeAddedOnThePlatformAppearsInTheMacro`, `s3_aMovedNodeHasTheSameStructureInTheMacro`, `s4_aNewPersonWithAccessIsAUserInTheMacroWithTheMappedRoleAndNode`

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
