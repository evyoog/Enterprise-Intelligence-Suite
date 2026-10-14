# TC-SUP-031: Security: only `MANAGE_SERVICE_STATUS` reaches incidents and problems

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-031 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-016](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
A user with no permissions and a platform administrator.

## Steps
1. Call `GET /admin/service-status/incidents`, `POST …/incidents`, `GET …/problems`, `POST …/problems` and a problem action with no token, then without the permission, then as an administrator.

## Expected Result
401, 403, then 200.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusAuthorizationTest.java` — "onlyAPlatformAdminCanPostStatus (the existing rule for `/admin/service-status/**`)"
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusSlaAuthorizationTest.java` — `instancesAndReporterKeysNeedManageServiceStatus`

The incident and problem list endpoints answer 401, 403 and 200 as above (same loop covers `/admin/service-status/incidents` and `/problems`).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
