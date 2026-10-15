# TC-TEN-083: Duplicate pending request is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-083 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.2, BR-AAC-003) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.onlyOnePendingRequestPerMemberAndProductAndCannotRequestWhatYouAlreadyHave`) |

## Preconditions
A member with a PENDING request for a product.

## Steps
1. The member requests the same product again while the first request is still PENDING.

## Expected Result
The second request is refused (`IllegalArgumentException`, message containing "pending"); the database's partial unique index (`uq_access_request_pending`) would also refuse a concurrent duplicate insert.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
