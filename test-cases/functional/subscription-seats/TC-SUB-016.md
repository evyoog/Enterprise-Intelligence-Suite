# TC-SUB-016: Adding a member beyond a subscription's seats is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-016 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63; named seats per product, C90) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
2 active members; the subscription set to 2 seats; licensed seats 10; a Manage billing user.

## Steps
1. Add a third member.
2. Raise the seats to 3; add the member again.

## Expected Result
The member joins, but the product stays off for them: turning it on is refused with "No free seats on …" (named seats per product, C90); after the increase the member who was left without a seat gets the product at once.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `aProductWithNoFreeSeatStaysOffForTheNextMemberUntilSeatsAreAdded`

## Actual Result
The automated test passed on 2026-10-14 (the test of 2026-10-03 was replaced by this one when seats became named per product).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
