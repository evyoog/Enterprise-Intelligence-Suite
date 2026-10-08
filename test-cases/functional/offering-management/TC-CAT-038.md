# TC-CAT-038: Audience rule: individuals only, organizations only, both

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-038 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active product with no rule.

## Steps
1. Check an individual and an organization member can both buy it.
2. Set Organizations only; check both buyer types.
3. Set Individuals only; check both.
4. Set both again.

## Expected Result
Open to everyone; then organizations only; then individuals only; then open again. Setting both removes the rule row.

## Automated coverage
- OfferingServiceTest — aProductIsOpenToEveryoneUntilARuleIsSet, audienceRuleBlocksTheOtherBuyerType; AdminOfferingsPage.test.tsx — saves a product rule

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
