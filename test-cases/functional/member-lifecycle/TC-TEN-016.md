# TC-TEN-016: Member Lifecycle & Access Review — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-016 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A member never reviewed.

## Steps
1. Arrange: an org with an admin and a member.
2. Act: the admin reviews the member's access.
3. Observe the member record.

## Expected Result
The member shows a non-null review timestamp.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanRecordAnAccessReview`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
