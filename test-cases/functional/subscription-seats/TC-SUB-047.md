# TC-SUB-047: A decrease waits for the next renewal, with no credit, and can be withdrawn

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-047 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Business rule(s) | [BR-2, BR-10, BR-11](../../../docs/02-requirements/FRD/subscription-seats/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with 5 seats and 1 member; a subscription with a renewal date; a Manage billing user.

## Steps
1. Set 3 seats.
2. Read the summary.
3. Set 5 seats again (the current quantity).
4. Set 5 again.

## Expected Result
Step 1: `effect: AT_RENEWAL`, the quantity stays 5, `pendingQuantity` is 3, **no invoice** is created and nothing is credited; the screen shows "Falls to 3 seats on {date}. Nothing is credited." with Withdraw, and the dialog says "You keep all 5 seats until your next renewal…". Step 3: `effect: WITHDRAWN`, `pendingQuantity` is null. Step 4: `effect: UNCHANGED`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `aDecreaseWaitsForTheNextRenewalWithNoCreditAndCanBeWithdrawn`
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "schedules a decrease for the next renewal with no estimate charge", "shows a pending decrease and withdraws it by asking for the current quantity"

Not covered by an automated test: the audit entries `SUBSCRIPTION_SEATS_DECREASE_SCHEDULED` and `SUBSCRIPTION_SEATS_DECREASE_WITHDRAWN`.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
