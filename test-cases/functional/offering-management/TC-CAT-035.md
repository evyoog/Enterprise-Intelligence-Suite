# TC-CAT-035: Publish needs an active product; customers see only published offerings

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-035 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A draft offering with an inactive and an active product.

## Steps
1. Remove the active product and try to publish.
2. Restore it and publish.
3. Open Offerings as a signed-out visitor.

## Expected Result
1. Refused. 2. Published. 3. The offering is listed with only its active product; a draft offering opens as not found.

## Automated coverage
- OfferingServiceTest — canBePublishedOnlyWithAnActiveProduct, publicViewShowsOnlyActiveOfferingsAndActiveProducts; OfferingsPage.test.tsx

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
