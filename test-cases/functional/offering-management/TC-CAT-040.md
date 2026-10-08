# TC-CAT-040: Subscribe and order cannot bypass the rule

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-040 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An organization-only product; an individual; an individual-only product; an organization member.

## Steps
1. The individual subscribes directly (API).
2. The organization member submits an order for the individual-only product.

## Expected Result
Both are refused with a message naming who the product is available to.

## Automated coverage
- OfferingServiceTest — audienceRuleBlocksTheOtherBuyerType, anIndividualCannotSubscribeDirectlyToAnOrganizationOnlyProduct (subscribe and subscribeFromCart). The organization order path (OrderService.submitOrder) has no automated test; it is a manual check

## Actual Result
The automated part passed on 2026-10-08; the manual part is not yet run.

## Status
Partly automated (2026-10-08); the rest is a manual check

## Linked Defect (if failed)
