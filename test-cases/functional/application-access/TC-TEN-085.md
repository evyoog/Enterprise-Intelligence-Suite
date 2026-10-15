# TC-TEN-085: Cannot request a product the member already has access to

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-085 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.1, BR-AAC-002) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.aMemberCanRemoveTheirOwnAccessImmediatelyAndMustRequestAgainToGetItBack`, request-after-approve step) |

## Preconditions
A member whose request for a product was already approved (ACTIVE `organization_product_access` row).

## Steps
1. The member calls `request(memberId, { productId: <already-accessible product>, message: null })` while their access is still ACTIVE.

## Expected Result
The request is refused (`IllegalArgumentException`, message containing "already have access"); no duplicate request or access row is created.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
