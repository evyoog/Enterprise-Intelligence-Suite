# TC-CAT-034: Refuse an offering with no product, an unknown product or a repeated name

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-034 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An offering named Bundle exists.

## Steps
1. Create an offering with no products.
2. Create one with a product that does not exist.
3. Create another named BUNDLE.

## Expected Result
Each is refused with a clear message; nothing is created.

## Automated coverage
- OfferingServiceTest — anOfferingNeedsAtLeastOneExistingProduct, namesAreUniqueIgnoringCase

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
