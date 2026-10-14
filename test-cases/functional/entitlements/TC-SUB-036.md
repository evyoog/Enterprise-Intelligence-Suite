# TC-SUB-036: My subscriptions shows what the active plans include, read-only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-036 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.2, .8); [screen](../../../docs/05-ui/screen-requirements/entitlements.md) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in customer with an active subscription (frontend test: the API is mocked).

## Steps
1. Open My subscriptions.
2. Read the "What your plans include" section.

## Expected Result
A card with the product heading, the plan, the included features ("Reports", "API access") and "Limit: 1,000"; no button, switch or usage figure on the card.

## Automated coverage
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "lists what the active plans include, read-only (C97 EN-1)"
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "has no detectable a11y violations"

Not covered by an automated test: the loading, empty ("You have no active subscriptions.") and error states.

## Actual Result
The automated tests below passed on 2026-10-13 (`vitest run`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
