# TC-INT-061: An edit made inside the Macro is saved on the platform first; revoked access and an ended subscription are denied

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-061 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-4, AC-7, AC-9](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
TC-INT-059 done; a person's Macro token (signed by the fake Keycloak) and the tenant's customer URL.

## Steps
1. As the administrator, `PUT /api/admin/users/{id}` in the Macro with a new first name.
2. As the member, call a screen; revoke the member's access on the platform; call again; grant again.
3. As the administrator, call a screen; set the subscription's end an hour in the past; call again; renew.

## Expected Result
The first name is held on the platform (version raised) before the Macro keeps it; afterwards the Macro holds the platform's version. With access revoked the Macro answers 403 `NO_PRODUCT_ACCESS`; with the subscription ended 403 `SUBSCRIPTION_ENDED`; after granting or renewing the person is let in again, each within seconds (the applied message drops the tenant's cache).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/toolsync/service/e2e/PlatformMacroEndToEndTest.java` `s5_aPersonEditedInTheMacroIsSavedOnThePlatformFirstAndThenEverywhere`, `s6_revokingProductAccessMakesTheMacroDenyThePerson`, `s7_anEndedSubscriptionIsDeniedByEveryone`

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
