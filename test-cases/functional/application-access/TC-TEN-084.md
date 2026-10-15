# TC-TEN-084: Cannot request a product the organization does not subscribe to

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-084 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.1, BR-AAC-001) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.onlyOnePendingRequestPerMemberAndProductAndCannotRequestWhatYouAlreadyHave`) |

## Preconditions
A member of an organization; an ACTIVE product the organization has **no** subscription to.

## Steps
1. The member calls `request(memberId, { productId: <unsubscribed product>, message: null })`.

## Expected Result
The request is refused (`IllegalArgumentException`, message containing "does not have an active subscription"); no `access_request` row is created. This is deliberately distinct from a purchase request ([REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md)), which is the correct path for a product the organization does not yet subscribe to.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
