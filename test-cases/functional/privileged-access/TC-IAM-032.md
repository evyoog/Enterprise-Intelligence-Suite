# TC-IAM-032: Privileged Access (User and Organization Administrator) — AC-10

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-032 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in user.

## Steps
1. Arrange: a signed-in user.
2. Act: they open the request form.
3. Observe the response and the UI.

## Expected Result
The permission dropdown lists only permissions that some role grants, never `MANAGE_PRIVILEGED_ACCESS`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "requestablePermissionsNeverIncludeTheApprovalPermission"
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "everyListedPermissionIsAcceptedByRequest"
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "offers the requestable permissions and submits a request"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
