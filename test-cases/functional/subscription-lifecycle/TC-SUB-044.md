# TC-SUB-044: My subscriptions shows the scheduled downgrade, Keep current plan and the plan rule

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-044 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.8, .10); [screen](../../../docs/05-ui/screen-requirements/my-subscriptions.md) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in customer with a subscription that has a scheduled downgrade (frontend test: the API is mocked).

## Steps
1. Open My subscriptions.
2. Press Keep current plan.
3. On another active subscription, open Change plan.

## Expected Result
The notice "Your plan changes to Basic on {date}, at your next renewal. You keep your current plan until then." shows. Keep current plan calls the withdraw endpoint and the notice disappears. The dialog says "An upgrade takes effect now. A downgrade takes effect at your next renewal."

## Automated coverage
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "shows a downgrade scheduled for the next renewal and lets the customer keep the current plan (C97 SC-1)"
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "explains the upgrade and downgrade rule in the change plan dialog (C97 SC-1)"

Not covered by an automated test: a scheduled-change notice for an organization subscription (not built; see the screen requirement).

## Actual Result
The automated tests below passed on 2026-10-13 (`vitest run`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
