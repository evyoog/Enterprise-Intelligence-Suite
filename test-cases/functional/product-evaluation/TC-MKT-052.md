# TC-MKT-052: An organization trial needs an administrator or Manage billing and has the organization's seats

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-052 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-006, BR-EVL-007](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
An organization with 5 licensed seats, an administrator and an ordinary member; a product with trial days.

## Steps
1. The ordinary member calls `POST /organization/me/trials`.
2. The administrator calls it.
3. The administrator calls it again for the same product.
4. A member who holds Manage billing (not an administrator) calls it for another product.

## Expected Result
Step 1: 403 "You do not have permission to do this". Step 2: a trial with `trial` true and 5 seats, owned by the organization. Step 3: 400 ("Your organization already has, or had, a subscription …"). Step 4: allowed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `anOrganizationTrialNeedsAnAdminOrManageBillingAndUsesTheOrganizationsSeats`
- `frontend/src/pages/ProductDetailPage.test.tsx` — "starts the trial for the organization when the customer may buy for it", "disables the trial when the organization already has a subscription", "tells an ordinary organization member to ask an administrator", "shows the organization trial refusal from the backend"

**Not covered by an automated test:** the Manage billing holder (step 4), and a caller who is not in an organization (404) or whose organization is not in good standing (403). The permission rule is `OrderService#canPurchaseDirectly`, which the purchase flow tests ([TC-MKT-040](../cart-checkout/TC-MKT-040.md) onwards).

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped. The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
