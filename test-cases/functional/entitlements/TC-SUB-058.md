# TC-SUB-058: A subscription in its grace period is allowed read-only, with the access mode and the grace end date

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-058 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (C100) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md), [AC-16](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Business rule(s) | [BR-1, BR-11](../../../docs/02-requirements/FRD/entitlements/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An individual with an ACTIVE subscription whose term ended with auto-renew off and whose grace end date is in the future; a second subscription within its term.

## Steps
1. Call the internal check `POST /internal/entitlements/check` for the ended subscription's product and read `GET /me/entitlements`.
2. Check the subscription that is within its term.
3. Turn auto-renew back on and check again.
4. Move the grace end date into the past, run the expiry job and check again.
5. Open My subscriptions and read the entitlement card of the read-only subscription.

## Expected Result
Step 1: `allowed: true`, `accessMode: READ_ONLY` and `graceEndsAt` equal to the subscription's grace end date; the entitlement list shows the same. Step 2: `accessMode: FULL` and `graceEndsAt` null. Step 3: `accessMode: FULL` again. Step 4: not allowed (`NO_SUBSCRIPTION`). Step 5: the card shows the chip "Read-only until {date}"; a full-access card shows none.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anEndedSubscriptionWithAutoRenewOffIsReadOnlyForSevenDaysThenExpires` (READ_ONLY with the grace end date in the check and in the list, then expired and not allowed), `turningAutoRenewBackOnDuringTheGracePeriodRenewsAtOnceAndRestoresFullAccess` (FULL again)
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "marks an entitlement read-only during the grace period (C100 RR-1)"

Not covered by an automated test: `accessMode: FULL` with a null `graceEndsAt` for a subscription within its term (the field's value is asserted only after renewal), `accessMode` and `graceEndsAt` on a `FEATURE_NOT_INCLUDED` answer, and the up-to-an-hour gap before the expiry job sets the grace end date (the check says `NO_SUBSCRIPTION` until then).

## Actual Result
The automated tests listed above passed on 2026-10-14. The parts listed as not covered have not been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
