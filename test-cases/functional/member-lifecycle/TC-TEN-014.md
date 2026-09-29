# TC-TEN-014: Member Lifecycle & Access Review — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-014 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with exactly one active ORG_ADMIN.

## Steps
1. Arrange: an org with one active admin.
2. Act: try to suspend, then try to remove, that admin.
3. Observe both responses.

## Expected Result
Both requests are refused.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `cannotSuspendOrRemoveTheOrganizationsLastAdmin`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
