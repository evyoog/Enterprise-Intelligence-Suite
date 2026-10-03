# TC-SUB-015: Seats cannot go below the seats in use

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-015 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
3 active members; 5 seats.

## Steps
1. Set 2 seats.
2. Set 3 seats.

## Expected Result
2 is refused (409) with "3 seats are in use. Remove members before reducing seats."; 3 succeeds; the organization seat limit stays 5. On screen, Save is disabled below the minimum.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `seatsCannotGoBelowTheSeatsInUse`
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "does not go below the seats in use"

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
