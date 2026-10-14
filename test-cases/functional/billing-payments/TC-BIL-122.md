# TC-BIL-122: The coupon API needs sign-in and the billing permission; the new endpoints need sign-in

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-122 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-51](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-29](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
No token; a user without `MANAGE_BILLING`; a user with it.

## Steps
1. Call `GET /admin/billing/coupons` with no token, as the plain user, and as the billing administrator.
2. As the billing administrator create a coupon (lower-case code), create it again, create one with percent 100, disable it, edit it to a fixed coupon, list, delete it, delete it again.
3. Call `PUT` and `DELETE /me/cart/coupon`, both auto-renew endpoints and both reminder-recipient endpoints without a token.

## Expected Result
Step 1: 401, 403, 200. Step 2: 201 with the code in capitals and status ACTIVE; 409; 400; the disable gives status DISABLED; the edit gives a FIXED coupon in INR; the list shows it; the first delete is 204 and the second 404; the plain user is 403 on POST and DELETE. Step 3: 401 for each.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/CouponAuthorizationTest.java` — `theCouponApiNeedsSignInAndTheBillingPermissionAndWorksEndToEnd`, `theNewCustomerEndpointsNeedSignIn`

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
