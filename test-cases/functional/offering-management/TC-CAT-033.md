# TC-CAT-033: Create an offering and keep the product order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-033 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator (MANAGE_CATALOG); two active products.

## Steps
1. Open Offerings and choose New offering.
2. Enter a name, tick the second product then the first, save.

## Expected Result
The offering is created as Draft and lists the products in the order they were added. The create is in the audit log.

## Automated coverage
- OfferingServiceTest — createsAnOfferingInDraftAndKeepsProductOrder; AdminOfferingsPage.test.tsx — creates an offering from a name and chosen products

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
