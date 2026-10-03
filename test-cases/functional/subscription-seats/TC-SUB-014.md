# TC-SUB-014: Increasing seats takes effect at once and is audited and published

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-014 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md), [AC-5](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization admin; 2 active members; 3 seats.

## Steps
1. My subscriptions → Organization subscriptions → + to 8 → Save → Change seats.

## Expected Result
Quantity 8 at once; the organization seat limit rises to 8; `SUBSCRIPTION_SEATS_CHANGED` is audited and `SeatsChanged` (from 3, to 8) is published; toast "Seats updated."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `anAdminSeesSeatsAndCanIncreaseThemAtOnce`
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "shows seats in use and changes the quantity after confirmation"

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
