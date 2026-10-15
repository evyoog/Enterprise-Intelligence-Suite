# TC-MKT-066: My subscriptions shows a trial with the days left and Subscribe, and no renewal controls

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-066 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-006, BR-EVL-009](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A running trial (10 days left), a trial with under a day left and an ended trial; an organization trial.

## Steps
1. Open My subscriptions.
2. Open the Organization subscriptions section for the organization trial.
3. Click Subscribe.

## Expected Result
A "Free trial" chip and "Ends {date} (10 days left)" ("1 day left" in the singular; "Ended {date}" for an ended trial); no auto-renew switch, no "Renews on", no Renew, no Change plan, no seat controls; Suspend stays; Subscribe goes to the product page `/products/{id}`.

## Automated coverage
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "shows the Free trial chip with the end date and days left, and no renew, auto-renew or change-plan controls", "Subscribe goes to the product page so the customer can buy", "says "1 day left" in the singular, and offers Subscribe (not Renew) for an ended trial"
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "shows a trial with its end and Subscribe, and no auto-renew, renewal or seat controls"

These are in the `free trial` block of `MySubscriptionsPage.test.tsx`. Buying from Subscribe goes through the cart check (TC-MKT-058), which now passes for a running trial.

## Actual Result
The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
