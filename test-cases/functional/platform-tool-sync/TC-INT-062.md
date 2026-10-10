# TC-INT-062: A stopped tool recovers by itself without losing anything; a deliberately deleted row is repaired by reconcile

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-062 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-2, AC-14](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
TC-INT-059 done.

## Steps
1. Stop the Macro; change the organization and add a node on the platform.
2. Observe the platform's messages; start the Macro again.
3. Delete a hierarchy node row in the Macro's database; reconcile the organization.

## Expected Result
While the tool is down the platform's messages are PENDING with attempts and a next try, none FAILED. After the restart the change and the node arrive by themselves and nothing is left waiting. Reconcile before the damage reports 'in step'; after the deletion it reports the OrgNode type out of step, resends exactly one object (the Macro forgot the version of the missing row, so the resend is applied, not 'duplicate'), the row is back and a second reconcile is in step.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/toolsync/service/e2e/PlatformMacroEndToEndTest.java` `s8_aToolThatIsDownRecoversByItselfWithoutLosingAnything`, `s9_aDeliberatelyDeletedRowIsFoundAndRepairedByReconcile`
- Macro: `StateDigestServiceTest`, `PlatformInboundPostgresTest.aDeletedProjectionRowIsSeenByTheDigestAndRepairedByTheResend`; platform: `ToolDeliveryTest.reconcile…`

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
