# TC-MKT-070: Regression: subscriptions, renewal, grace, seats, cart and organization purchase of a normal subscription are unchanged

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-070 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-009, BR-EVL-013, BR-EVL-015](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Yes |

## Preconditions
The existing automated suites for subscriptions.

## Steps
1. Run the backend suite (`cd backend && mvn -B verify`) and the frontend tests of My subscriptions and the cart.
2. Re-run the manual steps of [TC-SUB-001 to TC-SUB-012](../subscription-lifecycle/TC-SUB-001.md) and [TC-SUB-051 to TC-SUB-057](../renewal-reminders/TC-SUB-051.md) when the build changes them.

## Expected Result
Every existing test still passes: suspend, reactivate, cancel, renew, plan change and scheduled downgrade, auto-renew, the 7-day grace period and expiry, seats, the cart, the purchase request and the organization purchase all behave as before. A subscription with `trial` false is never touched by the trial rules.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — (the whole class)
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — (the whole class)
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — (the whole class)
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — (the whole class)
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — (the tests outside the `free trial` block)

These are existing tests, named here as the regression net; they were not changed for C102. The full backend run of 2026-10-15 had 876 tests, 0 failures and 28 skipped.

## Actual Result
The full backend run and the My subscriptions tests passed on 2026-10-15.

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
