# TC-SRM-012: Security: permission gating of instances, reporter keys and the organization list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-012 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-013](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
A user with no permissions, a platform administrator (ROLE_ADMIN), an organization administrator and an ordinary member.

## Steps
1. Call `GET /admin/service-status/instances`, `PUT /admin/service-status/instances/{id}`, `GET`, `POST` and `DELETE /admin/service-status/reporter-keys` with no token, then with a user without `MANAGE_SERVICE_STATUS`, then as an administrator.
2. Call `GET /organization/me/service-instances` with no token, as an ordinary member, and as an organization administrator.

## Expected Result
Step 1: 401, 403, then 200 (or 404 for an unknown id). Step 2: 401, 403, 200.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusAuthorizationTest.java` — "onlyAPlatformAdminCanPostStatus (the existing rule for `/admin/service-status/**`: 401 without a token, 403 without the permission, 200 as ROLE_ADMIN)"
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `anOrganizationAdminSeesOnlyTheirOrganizationsInstancesAndAMemberCannot (the organization check in the service)`
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusSlaAuthorizationTest.java` — `instancesAndReporterKeysNeedManageServiceStatus`

The new instance, summary and reporter-key endpoints answer 401 without a token, 403 without the permission and 200 for a platform admin; `/organization/me/service-instances` answers 401 without a token (`anOrganizationsInstancesNeedASignedInCustomer`).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
