# TC-BIL-123: The Coupons page lists, adds, edits, enables, disables and deletes coupons

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-123 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-52](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-24, BR-25](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional, Accessibility |
| Automated | Yes |

## Preconditions
A billing administrator on `/admin/billing/coupons`; a list of coupons; an empty list; a coupon that has been redeemed.

## Steps
1. Open the page with coupons and with none.
2. Add a percent coupon (code in lower case) and a fixed coupon in USD of 10.00.
3. Submit the form empty and with a percent of 100.
4. Edit a coupon (the code field is locked).
5. Disable an active coupon and enable a disabled one.
6. Delete a coupon and confirm; try to delete a redeemed coupon.
7. Make the list fail to load.

## Expected Result
Step 1: the table shows code, discount, expiry, status chip and redeemed count; the empty state offers Add coupon. Step 2: the code is sent in capitals and the expiry as an ISO instant in the browser's zone; the fixed amount is sent in minor units (1000) with its currency. Step 3: the form shows field errors and sends nothing. Step 4: the code is read-only. Step 5: the row updates and a toast confirms. Step 6: a confirmation is asked first; for a redeemed coupon the server's message is shown and the row stays. Step 7: an alert with **Retry**. No axe violations.

## Automated coverage
- `frontend/src/pages/admin/AdminCouponsPage.test.tsx` — "lists each coupon with its discount, status and redeemed count, and has no accessibility violations", "shows an empty state with an Add coupon action", "creates a percent coupon with the code in capitals and the expiry as an ISO instant in the browser time zone", "converts a fixed amount in major units to minor units with its currency", "checks the form before sending and shows what the server refuses", "edits a coupon with the code locked", "disables an active coupon and enables a disabled one", "deletes a coupon only after confirmation", "shows the message when the server refuses to delete a redeemed coupon", "offers a retry when the list cannot be loaded"

Not covered by an automated test: the sidebar entry (`appShell.nav.coupons`, `navConfig`) being shown only with `MANAGE_BILLING`, and the page refusing a past expiry when editing while the API accepts it (see the FRD open points).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
