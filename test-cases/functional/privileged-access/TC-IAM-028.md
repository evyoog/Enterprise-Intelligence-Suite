# TC-IAM-028: Privileged Access (User and Organization Administrator) — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-028 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A pending request.

## Steps
1. Arrange: a pending request.
2. Act: the administrator approves it.
3. Observe the response and the UI.

## Expected Result
Its status is APPROVED, its expiry is approval time plus the requested duration, and the requester is notified.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "fullLifecycle_requestApproveGrantThenExpire"
- `frontend/src/components/organization/OrganizationPrivilegedAccessCard.test.tsx` — "approves a pending request with a note and reloads"

**Manual check:** Check the requester receives the approval notification.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
