# TC-INT-059: A new organization is provisioned in the real Macro and everything arrives within seconds

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-059 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-1, AC-12](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional (end to end where stated) |
| Automated | Yes |

## Preconditions
The real platform application, the Macro Planner jar as a separate process on a scratch PostgreSQL, a fake Keycloak that signs real tokens; a tool connector row; an organization with a hierarchy, two people, memberships and product access.

## Steps
1. Give the organization an ACTIVE subscription to the tool's product (its last day six months ahead).
2. Wait for the tenant to be READY in the platform's registry; read the Macro's control and tenant schemas.

## Expected Result
The platform asks the Macro to provision a tenant for the organization (the Macro registers it, creates the schema `pms_<ref>`, migrates it, marks it READY), then sends the organization, the hierarchy, both people with their memberships, the subscription (end 18:29:00 UTC = 23:59:00 in India) and each person's access. Measured: 5.0–5.4 s from subscription to a completely synchronized tenant. The person with `PMS_USER` has the Macro's least role (`TASK_USER`) and sits on the node the platform placed them. No project or other business record exists in the tenant.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/toolsync/e2e/PlatformMacroEndToEndTest.java` `s1_aSubscriptionProvisionsATenantAndEverythingArrivesWithinSeconds`

## Actual Result
Passed on 2026-10-10. The end-to-end test is opt-in (`E2E_MACRO_JAR`, see [operations](../../../docs/09-integrations/platform-tool-operations.md) section 5) and is **not** part of CI. It ran against a fake Keycloak, a local PostgreSQL 16 and a Macro jar built from the same branch, not against a staging environment.

## Status
Passed (automated run 2026-10-10)

## Linked Defect (if failed)
None.
