# TC-BIL-119: A platform administrator creates, edits, disables and deletes coupons, within the rules

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-119 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-48](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-24, BR-25, BR-26](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A user with `MANAGE_BILLING`; no coupon with the code SAVE-20.

## Steps
1. Create the code "save-20" (percent 20, expiry tomorrow).
2. Create the same code again; try percent 100, an expiry in the past, a one-character code and a fixed coupon without a currency.
3. Disable it; ask for a quote; enable it.
4. Edit its expiry into the past; ask for a quote.
5. Create a fixed coupon of 50.00 USD and ask for quotes in INR, in USD for a cart of 1,000.00 and in USD for a cart of 50.00.
6. Delete the fixed coupon (never redeemed).

## Expected Result
Step 1: stored as `SAVE-20`, status ACTIVE. Step 2: the duplicate is 409; the others are refused with "…between 1% and 99%", "…must be in the future", "…3 to 40 letters…", "…a three-letter currency". Step 3: DISABLED and "This coupon is not active."; enabling restores ACTIVE. Step 4: EXPIRED and "This coupon has expired." An unknown code is "This coupon code is not valid." Step 5: "This coupon is for USD purchases only.", the discount 50.00 on 1,000.00, and "This coupon is worth more than your cart. Add more to your cart to use it." for 50.00. Step 6: removed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CouponAndPurchaseTest.java` — `theAdministratorCreatesDisablesAndDeletesCouponsAndTheRulesAreEnforced`

Not covered by an automated test: the audit entries (`COUPON_CREATED`, `COUPON_UPDATED`, `COUPON_ENABLED`, `COUPON_DISABLED`, `COUPON_DELETED`) and that a redeemed coupon cannot be deleted at the service level (the invoice case in TC-BIL-120 asserts it).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
