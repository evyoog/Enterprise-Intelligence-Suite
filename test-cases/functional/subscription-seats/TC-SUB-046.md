# TC-SUB-046: An increase takes effect now and is charged now for the remaining days

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-046 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Business rule(s) | [BR-5, BR-7, BR-9](../../../docs/02-requirements/FRD/subscription-seats/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with 5 seats and 2 members; a Monthly plan at 1,000.00 per seat; the term ends in 15 days; a Manage billing user.

## Steps
1. My subscriptions → Organization subscriptions → set 8 seats.
2. Read the estimate, then Save → Change seats.
3. Read the result and open the invoice.

## Expected Result
Step 2: before saving the screen shows "Charged now for 15 of 30 days: 1,500.00" (3 added seats) and the dialog says the added seats are charged on a new invoice. Step 3: the quantity is 8 at once; the answer has `effect: NOW` and an `invoiceId`; one invoice exists whose subtotal is 3 added seats × price × remaining days ÷ 30, on one line "Added seats: …" with quantity 3; the organization seat limit rises to 8; the audit entry `SUBSCRIPTION_SEATS_CHANGED` and the event `SeatsChanged` (from 5 to 8) exist; the screen shows "Seats increased to 8. An invoice was created for the added seats." with a View invoices link.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anIncreaseIsEffectiveAtOnceAndChargedForTheRestOfTheTerm`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `anAdminSeesSeatsAndCanIncreaseThemAtOnce` (limit raised, audit, `SeatsChanged`)
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "shows seats in use and an estimate for an increase, then the result with an invoice link"

Not covered by an automated test: tax on the seat-increase invoice (see TC-SUB-050).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
