# TC-CAT-041: Works with list on the product page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-041 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Product A works with product B (active) and C (inactive).

## Steps
1. Save A's rules with B and C.
2. Open A as a visitor.
3. Clear the list.

## Expected Result
Only B is shown, linking to B's page. After clearing, no Works with section.

## Automated coverage
- OfferingServiceTest — worksWithListIsReplacedAndPublicShowsActiveProductsOnly; ProductDetailPage.test.tsx

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
