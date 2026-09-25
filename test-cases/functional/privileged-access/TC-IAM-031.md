# TC-IAM-031: Privileged Access (User and Organization Administrator) — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-031 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The requester's own PENDING or active request.

## Steps
1. Arrange: the requester's own PENDING or active request.
2. Act: they withdraw it.
3. Observe the response and the UI.

## Expected Result
Its status is REVOKED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "requesterCanWithdrawTheirOwnPendingRequest"
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "shows an expired grant and lets the requester withdraw a pending one"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
