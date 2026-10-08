# TC-CAT-039: Cart blocks a product that is not for the buyer type

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-039 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An individual with an organization-only product in the cart.

## Steps
1. Validate the cart.
2. Try to check out.

## Expected Result
The item shows Not available for your account type; checkout is refused until it is removed.

## Automated coverage
- CartServiceTest — anOrganizationOnlyProductIsFlaggedNotEligibleForAnIndividual; CartPage.test.tsx

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
