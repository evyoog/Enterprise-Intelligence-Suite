# TC-SUB-054: A mid-term seat increase on a seat-pricing-enabled product bills the prorated add-on only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-054 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C106) |
| Decision | [C106](../../../docs/01-business/roadmap/open-decisions.md#c106) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional (automated) |
| Automated | Yes — `SeatPricingTest.aMidTermSeatIncreaseBillsOnlyTheProratedSeatAddonForASeatPricingEnabledProduct` |

## Preconditions
A seat-pricing-enabled product, per-seat price $20/month; an organization subscription with 2 seats, full 30-day term remaining.

## Steps
1. Increase seats by 3 (2 → 5) with the full term remaining.
2. Open the resulting invoice.

## Expected Result
One line only (the seat add-on), quantity 3, amount $60 (3 x $20, full remaining term — no base line on a seat-increase invoice, matching BR-9's existing proration approach applied to the add-on only).

## Automated coverage
`backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/SeatPricingTest.java` — `aMidTermSeatIncreaseBillsOnlyTheProratedSeatAddonForASeatPricingEnabledProduct`. **Passed.**

## Actual Result
Passed (automated).

## Status
Passed

## Linked Defect (if failed)
None.
