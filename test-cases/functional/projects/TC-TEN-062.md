# TC-TEN-062: Projects — assign resources (products and roles)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-062 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.3, BR-PRJ-003) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.aProductNeedsAnActiveSubscriptionAnActiveProductAndARole`) |

## Steps
1. Add a product the organization does not subscribe to, a retired product, a product with a blank role and no product.
2. Add a subscribed active product with a role, then add it again with a new role.

## Expected Result
The first four are refused (400) with the matching message. The product is added with its role, and the second call changes the role without a duplicate.

## Status
Passed (automated, 2026-10-12).
