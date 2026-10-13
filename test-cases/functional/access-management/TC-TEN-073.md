# TC-TEN-073: Access management — Named seats per product

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-073 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005.5, BR-8 (AM-3 B)) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagementServiceTest.namedSeatsLimitAProductAndAFreedSeatIsFilledByTheNextRecalculation`, `SubscriptionSeatServiceTest.aProductWithNoFreeSeat...`) |

## Steps
1. A product has 2 seats; add an admin and two members.
2. Turn the product ON for the member without a seat.
3. Turn it OFF for another member and ON for this one; suspend a member; add seats.

## Expected Result
Only two members hold the product; the third member shows "No free seats on {product}" and the grant is refused (409). Freeing a seat or adding seats lets the next member have it; a suspended member frees theirs; the platform seat-overage switch removes the limit.

## Status
Passed (automated, 2026-10-12).
