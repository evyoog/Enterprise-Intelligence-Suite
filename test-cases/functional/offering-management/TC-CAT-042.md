# TC-CAT-042: Bad rule input and deleted products

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-042 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two products.

## Steps
1. Make a product work with itself.
2. Use an unknown audience.
3. Save a rule for an unknown product.
4. Delete a product that others work with.

## Expected Result
1–3 refused. 4: its rule and every works-with row naming it are gone.

## Automated coverage
- OfferingServiceTest — rulesRejectBadInput, rulesFollowADeletedProduct

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
