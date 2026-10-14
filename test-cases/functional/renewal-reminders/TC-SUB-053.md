# TC-SUB-053: Turning auto-renew back on during the grace period renews at once

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-053 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-14](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An individual subscription in its grace period (term ended 2 days ago, auto-renew off).

## Steps
1. Switch Auto-renew on.
2. Read the subscription and the entitlement check.
3. Open My subscriptions in a state with a grace banner, then after the grace period has passed.

## Expected Result
Step 1: the subscription renews at once; the grace end date is cleared and the term end is in the future. Step 2: `accessMode: FULL`. Step 3: while in grace the card shows the "Read-only until {date}" chip and a notice with **Renew now** (an individual card); once auto-renew is on, or once the grace period has passed, they are gone.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `turningAutoRenewBackOnDuringTheGracePeriodRenewsAtOnceAndRestoresFullAccess`
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "shows a read-only banner with Renew while the grace period runs, and leaves it once auto-renew is on again (C100 RR-1)", "renews from the grace banner", "does not show a banner once the grace period has passed"

Not covered by an automated test: an organization subscription in grace (it has no Renew now button; only the auto-renew switch).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
