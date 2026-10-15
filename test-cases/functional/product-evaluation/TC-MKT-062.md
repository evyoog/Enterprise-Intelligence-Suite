# TC-MKT-062: The tax estimate: not signed in, no billing details, estimated, or at checkout

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-062 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-026, BR-EVL-027](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Two published products; a visitor; a signed-in customer with no billing details; a signed-in customer with billing details (country and state) and an active EIS tax rule; the same with the external tax provider on.

## Steps
1. Compare the two products as the visitor.
2. Compare as the customer with no billing details.
3. Compare as the customer with billing details and a tax rule for their region.
4. Turn the external tax provider on and compare again; then use billing details that no rule covers.

## Expected Result
Step 1: `NOT_SIGNED_IN` ("Sign in to see the tax estimate."). Step 2: `NEEDS_BILLING_DETAILS`. Step 3: `ESTIMATED` with the tax from the rule and the total = subtotal + tax. Step 4: `AT_CHECKOUT` ("Tax is worked out at checkout."). On the compare page each status shows its link or text and the estimate.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `comparingPlansMultipliesThePriceBySeatsInEachItemsOwnCurrency`, `aSignedInCustomerWithoutBillingDetailsIsToldToAddThem`
- `frontend/src/pages/ComparePage.test.tsx` — "reads the items and seats from the URL, calls the API and shows price x seats and the tax estimate", "shows each tax status: sign in, billing details, and tax worked out at checkout"

**Not covered by an automated test:** `ESTIMATED` and `AT_CHECKOUT` in the backend (`CompareService#tax` with `TaxRuleService#preview`, the external-provider switch, a sale the rules cannot tax, a zero subtotal, an organization member). The page tests use a mocked API. **Not Run** by hand.

## Actual Result
The `NOT_SIGNED_IN` and `NEEDS_BILLING_DETAILS` backend cases and the page tests passed in the automated run of 2026-10-15. The other tax cases were not run.

## Status
Partly passed: automated run 2026-10-15 for two of four statuses; ESTIMATED and AT_CHECKOUT Not Run

## Linked Defect (if failed)
None.
