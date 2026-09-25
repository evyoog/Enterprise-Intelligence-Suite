# TC-IAM-023: Privileged Access (User and Organization Administrator) — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-023 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in organization member.

## Steps
1. Arrange: a signed-in organization member.
2. Act: they request `MANAGE_USERS` for 60 minutes with a justification.
3. Observe the response and the UI.

## Expected Result
The request appears in "My requests" as PENDING.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "fullLifecycle_requestApproveGrantThenExpire"
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "offers the requestable permissions and submits a request"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
