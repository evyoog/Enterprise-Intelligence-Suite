# TC-MKT-054: A trial cannot be renewed, set to auto-renew or moved to another plan; an organization trial cannot change seats

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-054 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-009](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A running trial of an individual; a running trial of an organization.

## Steps
1. As the individual: `POST /me/subscriptions/{id}/renew`, `PATCH /me/subscriptions/{id}/auto-renew` (on), `PATCH /me/subscriptions/{id}/plan` to the top plan.
2. As the organization administrator: `PATCH /organization/me/subscriptions/{id}/auto-renew`, `PATCH …/plan`, `PATCH …/seats`.
3. Suspend and cancel the individual trial.

## Expected Result
Steps 1 and 2: each is 400 with a message that says to subscribe ("A free trial cannot be renewed.", "A free trial does not renew.", "A free trial has all features on the highest plan.", "A free trial has the organization's seats."). Step 3: works as for any subscription.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aTrialCannotBeRenewedAutoRenewedOrChangedToAnotherPlan`

The test covers the individual renew, auto-renew and plan change. **Not covered by an automated test:** the organization auto-renew, plan and seat refusals (`changeOrganizationPlan`, `changeAutoRenew`, `SubscriptionSeatService#changeSeats`) and suspend and cancel of a trial. **Not Run** by hand.

## Actual Result
The individual part passed in the automated run of 2026-10-15; the organization part was not run.

## Status
Partly passed: individual guards automated run 2026-10-15; organization guards Not Run

## Linked Defect (if failed)
None.
