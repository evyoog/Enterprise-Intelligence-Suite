# TC-IAM-025: Privileged Access (User and Organization Administrator) — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-025 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A request for `MANAGE_PRIVILEGED_ACCESS`.

## Steps
1. Arrange: a request for `MANAGE_PRIVILEGED_ACCESS`.
2. Act: it is submitted.
3. Observe the response and the UI.

## Expected Result
The response is 400 and no request is created.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "cannotRequestTheApprovalPermissionItself"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
