# TC-SUB-053: An organization's initial invoice carries a base line and a seat-addon line

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-053 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C106) |
| Decision | [C106](../../../docs/01-business/roadmap/open-decisions.md#c106) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional (automated) |
| Automated | Yes — `SeatPricingTest.anOrganizationsInitialInvoiceHasABaseLineAndASeatAddonLineForFiveSeats` |

## Preconditions
A seat-pricing-enabled product, base price $200/month, per-seat price $20/month; an organization with 5 seats subscribing.

## Steps
1. The organization subscribes (5 seats).
2. Open the generated invoice.

## Expected Result
Two lines: a base line (quantity 1, amount $200) and a seat-addon line (quantity 5, amount $100); subtotal $300 — matching Total = Base + (per-seat price x seats). A non-seat-pricing product's invoice is unaffected (still one `unit x seats` line, as before this feature).

## Automated coverage
`backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/SeatPricingTest.java` — `anOrganizationsInitialInvoiceHasABaseLineAndASeatAddonLineForFiveSeats`. **Passed.**

## Actual Result
Passed (automated).

## Status
Passed

## Linked Defect (if failed)
None.
