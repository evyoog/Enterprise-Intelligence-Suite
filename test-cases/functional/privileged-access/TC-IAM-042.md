# TC-IAM-042: Privileged Access — AC-13

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-042 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved, unexpired ORGANIZATION-scope grant.

## Steps
1. Arrange: an approved, unexpired ORGANIZATION-scope grant.
2. Act: the organization administrator opens the approvals card.
3. Observe the response and the UI.

## Expected Result
It is listed under Active grants with the requester's email and expiry, and not for another organization or the platform list.

## Automated coverage
- `backend/…/PrivilegedAccessServiceTest.java` — "activeGrantsAreListedForTheirOwnScopeUntilRevokedOrExpired"
- `frontend/src/components/organization/OrganizationPrivilegedAccessCard.test.tsx` — "lists active grants with who holds them and revokes one early"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
