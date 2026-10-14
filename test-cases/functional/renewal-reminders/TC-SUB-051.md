# TC-SUB-051: Auto-renew can be turned off and on, with a confirmation, for individuals and organizations

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-051 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-11](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional, Security |
| Automated | Partly |

## Preconditions
An individual with a Monthly subscription; an organization subscription; an organization administrator, a member with Manage subscriptions and a plain member; a subscription with no renewal date.

## Steps
1. As the individual, My subscriptions → switch Auto-renew off.
2. Confirm the dialog; then switch it on.
3. Try the switch on the subscription that has no renewal date (through the API).
4. As an organization member with Manage billing, then with Manage subscriptions, then a plain member, call `PATCH /organization/me/subscriptions/{id}/auto-renew`.
5. Call both PATCH endpoints without a token.

## Expected Result
Step 1: a dialog "Turn off auto-renew for {product}?" explains the 7-day read-only grace period; nothing changes until **Turn off auto-renew** is chosen. Step 2: the switch is off, the hint says "Not renewing…"; turning it on is immediate (no dialog). Each change is audited (`SUBSCRIPTION_AUTO_RENEW_CHANGED`) and publishes `SubscriptionChanged`. Step 3: 400 "This subscription has no renewal date." Step 4: Manage billing and Manage subscriptions can change it; the plain member gets 403. Step 5: 401.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anEndedSubscriptionWithAutoRenewOffIsReadOnlyForSevenDaysThenExpires`, `turningAutoRenewBackOnDuringTheGracePeriodRenewsAtOnceAndRestoresFullAccess` (the individual switch off and on through the service)
- `backend/src/test/java/com/vyoog/eisplatform/config/CouponAuthorizationTest.java` — `theNewCustomerEndpointsNeedSignIn` (401 for both auto-renew endpoints)
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "turns auto-renew off after a confirmation that explains the 7-day read-only grace period (C100 RR-1)", "turns auto-renew on straight away", "shows the server message when auto-renew cannot be changed"
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "confirms before turning auto-renew off, explaining the 7-day read-only grace period"

Not covered by an automated test: the audit entry and the `SubscriptionChanged` event of the switch, the refusal for a subscription with no renewal date, and the organization permission check (Manage billing and Manage subscriptions allowed, a plain member refused). Those parts are **Not Run**.

## Actual Result
The automated tests listed above passed on 2026-10-14. The parts listed as not covered have not been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
