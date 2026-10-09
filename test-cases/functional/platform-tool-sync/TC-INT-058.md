# TC-INT-058: The authoritative entitlement answer, and the provisioning report that starts the first full send

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-058 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-7, AC-12](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A tenant PENDING then READY; a member, a subscription and access in turn. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Ask `get_entitlement` after each step: tenant pending; unknown person; no subscription row; subscription without access; access granted; subscription ended (expired time, then SUSPENDED); member suspended; organization suspended; another product code.
2. An organization gets an ACTIVE subscription and no tenant: deliver; the tool provisions; then the full send; also the tool's `report_provisioning_result` (FAILED then READY); and a report for an organization never asked about.

## Expected Result
Reasons come in the contract's order: `NOT_PROVISIONED`, `NOT_A_MEMBER`, `NO_SUBSCRIPTION`, `NO_PRODUCT_ACCESS`, then `OK` with `productRole` and `endsAt` ending `T23:59:00.000+05:30`; `SUBSCRIPTION_ENDED` (also while SUSPENDED), `USER_INACTIVE`, `ORG_INACTIVE`; a question for another product is `NOT_ALLOWED_CLIENT`. A subscription event for an organization without a tenant creates a PENDING tenant and one `provision_tenant` request with the first active ORG_ADMIN that has a Keycloak id (never a database or schema name; waits with `NO_FIRST_ADMIN` until there is one; a rejection makes the tenant FAILED); on success the tenant is READY with its schema version and the organization, hierarchy (parents first), people, memberships, the tool's subscription and the people's access are sent. The tool's own READY/FAILED report records the same state once; an organization the platform never asked about is `UNKNOWN_TENANT`.

## Automated coverage
- `.../toolsync/service/ToolInboundTest.theEntitlementAnswerGivesTheReasonInTheOrderOfTheContract`, `.anEntitlementQuestionIsAnswerableOnlyForTheCallersOwnProduct`, `.theToolsProvisioningReportMakesTheTenantReadyAndStartsTheFullSend`, `.aProvisioningReportForAnOrganizationThePlatformNeverAskedAboutIsRefused`
- `.../toolsync/service/ToolDeliveryTest.aSubscriptionWithoutATenantStartsProvisioningAndASuccessSendsEverything`, `.provisioningWaitsForAnAdministratorAndFailsWhenTheToolRejectsIt`

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
