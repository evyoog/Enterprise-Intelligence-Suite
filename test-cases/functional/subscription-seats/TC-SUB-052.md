# TC-SUB-052: Catalog card shows base + first seat, with the add-on messages

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-052 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C106) |
| Decision | [C106](../../../docs/01-business/roadmap/open-decisions.md#c106) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
A product with seat pricing enabled, base price $200/month, per-seat price $20/month (platform default or its own override).

## Steps
1. Open the public catalog.
2. Find the product's card.

## Expected Result
The card's starting price reads **$220/month** (base + the first seat, not free); below it, **"1 User License Included"** and **"Additional users: $20/user/month"** are shown. A product with seat pricing disabled shows only its base price, unchanged (no new messaging).

## Automated coverage
None (visual/catalog-card rendering). The underlying price math ($200 + $20 x seats) is covered by `SeatPricingTest` (cart) and `CatalogProductCard` price computation is exercised indirectly by existing catalog tests, but the exact "1 User License Included" / "Additional users" copy is not asserted automatically. **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
