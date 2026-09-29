# TC-IAM-043: Privileged Access — AC-14

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-043 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active grant.

## Steps
1. Arrange: an active grant.
2. Act: an organization or platform administrator revokes it with a note.
3. Observe the response and the UI.

## Expected Result
It disappears from Active grants and no longer grants the permission; a grant that is no longer active is refused with the backend message.

## Automated coverage
- `backend/…/PrivilegedAccessServiceTest.java` — "activeGrantsAreListedForTheirOwnScopeUntilRevokedOrExpired"
- `frontend/src/pages/admin/AdminPrivilegedAccessPage.test.tsx` — "revokes an active platform grant with a note", "shows the backend message when a grant is no longer active"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
