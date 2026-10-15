# TC-TEN-081: Admin approves a request, granting access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-081 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.5, BR-AAC-005) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.aMemberRequestsAccessAndAnAdminApprovesIt`) |

## Preconditions
A PENDING request from a member for a product the organization subscribes to.

## Steps
1. The organization admin calls `approve(adminId, requestId)`.
2. Read the member's `organization_product_access` row for that product.

## Expected Result
The request's status becomes APPROVED, and the member's product access row is ACTIVE (granted through `AccessSyncService#grantProduct`, the same path as a direct administrator grant, including the seat check).

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
