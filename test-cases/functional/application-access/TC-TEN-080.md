# TC-TEN-080: Member submits an access request

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-080 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.1, BR-AAC-001) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.aMemberRequestsAccessAndAnAdminApprovesIt`) |

## Preconditions
An organization with an ORG_ADMIN and a MEMBER; a product with an ACTIVE subscription owned by the organization; the member does not yet have access to it.

## Steps
1. The member calls `request(memberId, { productId, message })`.
2. Read the created request back through `myRequests` (member) and `listForOrganization` (admin).

## Expected Result
A request is created with status PENDING, the member's message is stored, and it appears in both the member's own list and the organization's admin list.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
