# TC-SUB-051: Global seat price change updates an inheriting product but not an overridden one

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-051 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C106) |
| Decision | [C106](../../../docs/01-business/roadmap/open-decisions.md#c106) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional (automated) |
| Automated | Yes — `SeatPricingTest.aProductsOwnSeatPriceOverridesTheGlobalDefaultWithoutChangingOtherProducts` |

## Preconditions
A platform administrator; two products with seat pricing enabled — one with no seat-price override, one with its own override.

## Steps
1. Platform Admin → Billing → Seat pricing: set the global default per-seat price to $20.
2. Confirm both products' effective seat price.
3. Change the global default to $25.
4. Confirm both products' effective seat price again.

## Expected Result
Before the change, the inheriting product shows $20 and the overridden product shows its own price (unaffected by the default). After the change, the inheriting product shows $25; the overridden product is unchanged.

## Automated coverage
`backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/SeatPricingTest.java` — `aProductsOwnSeatPriceOverridesTheGlobalDefaultWithoutChangingOtherProducts`. **Passed.**

## Actual Result
Passed (automated).

## Status
Passed

## Linked Defect (if failed)
None.
