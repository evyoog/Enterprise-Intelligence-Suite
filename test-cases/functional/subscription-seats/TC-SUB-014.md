# TC-SUB-014: Increasing seats takes effect at once and is audited and published

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-014 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63; changed by C100: the increase is also charged, see TC-SUB-046) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md), [AC-5](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization administrator (Manage billing); 2 active members; 3 seats on a subscription with no price (or see TC-SUB-046 for the charge).

## Steps
1. My subscriptions → Organization subscriptions → + to 8 → Save → Change seats (the dialog says the added seats take effect now and are charged on a new invoice).

## Expected Result
Quantity 8 at once; the organization seat limit rises to 8; `SUBSCRIPTION_SEATS_CHANGED` is audited and `SeatsChanged` (from 3, to 8) is published; the screen shows "Seats changed to 8." ("Seats increased to 8. An invoice was created for the added seats." when there is a price)

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `anAdminSeesSeatsAndCanIncreaseThemAtOnce`
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "shows seats in use and an estimate for an increase, then the result with an invoice link"

## Actual Result
The automated tests passed on 2026-10-03 and again on 2026-10-14 after the C100 change (the frontend test was renamed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
