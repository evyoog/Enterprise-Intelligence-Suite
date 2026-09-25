# TC-IAM-027: Privileged Access (User and Organization Administrator) — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-027 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An administrator reviewing their own request.

## Steps
1. Arrange: an administrator reviewing their own request.
2. Act: they approve or reject it.
3. Observe the response and the UI.

## Expected Result
The response is 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "anApproverCannotApproveTheirOwnRequest"
- `frontend/src/components/organization/OrganizationPrivilegedAccessCard.test.tsx` — "shows the backend refusal of a self-approval"

**Manual check:** Check self-rejection is also refused (403).

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
