# TC-SUB-045: The plan price is per seat: invoices bill price × seats

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-045 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Business rule(s) | [BR-8](../../../docs/02-requirements/FRD/subscription-seats/business-rules.md), [BR-30](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with 5 seats and a Monthly plan priced 1,000.00 per seat; Manage billing user (an organization administrator).

## Steps
1. Open the seat summary of the organization subscription.
2. Schedule a decrease to 3 seats, move the term end into the past and run the renewal job.
3. Read the renewal invoice and its line.
4. As an organization buyer, put two products (40.00 and 60.00 per seat) in the cart.

## Expected Result
Step 1: the summary shows the price of one seat (100,000 in minor units) and a 30-day term. Step 3: the renewal invoice subtotal is 3 × 1,000.00 = 3,000.00 and its line has quantity 3. Step 4: the cart shows each item with the organization's 5 seats, the subtotal is (40.00 + 60.00) × 5, and the invoice created at checkout equals the cart total. An individual subscription is billed one price (quantity 1).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anIncreaseIsEffectiveAtOnceAndChargedForTheRestOfTheTerm` (unit amount and term days in the summary), `theScheduledDecreaseTakesEffectWithTheRenewalAndIsBilledForFewerSeats` (3 × 1000, line quantity 3)
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `anAdminOrAManageBillingHolderBuysDirectlyAndTheAdminsAreToldWhenItIsNotAnAdmin` (5 seats per item, subtotal and invoice = cart total)
- `frontend/src/pages/CartPage.test.tsx` — "shows the seats and the per-seat price of an organization item, and nothing for one seat"

Not covered by an automated test: the quantity on the invoice line of a **new purchase** (the renewal invoice's quantity is asserted). Per-seat pricing is an engineering default the product owner has to confirm.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
