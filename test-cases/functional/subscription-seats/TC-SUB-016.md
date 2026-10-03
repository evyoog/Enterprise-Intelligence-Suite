# TC-SUB-016: Adding a member beyond a subscription's seats is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-016 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
2 active members; the subscription set to 2 seats; licensed seats 10.

## Steps
1. Add a third member.
2. Raise the seats to 3; add the member again.

## Expected Result
The first add is refused (seat limit); after the increase it succeeds.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `addingAMemberBeyondASubscriptionsSeatsIsRefused`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
