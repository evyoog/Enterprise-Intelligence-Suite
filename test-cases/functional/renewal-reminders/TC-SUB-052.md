# TC-SUB-052: A term that ends with auto-renew off is read-only for 7 days, then expires

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-052 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-12, BR-13](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An individual with an ACTIVE subscription whose term ended yesterday with auto-renew off; a second subscription whose term has not ended, also with auto-renew off.

## Steps
1. Run the expiry job.
2. Call the internal entitlement check and list the customer's entitlements.
3. Move the grace end date into the past and run the job again.
4. Check again.

## Expected Result
Step 1: nothing expires; the ended subscription stays ACTIVE with a grace end date of the term end + 7 days; the other has no grace end date. Step 2: `allowed: true`, `accessMode: READ_ONLY`, `graceEndsAt` set, and the list shows the entitlement as READ_ONLY. Step 3: the job expires it (status EXPIRED); the one that had not ended is still ACTIVE. Step 4: not allowed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `expiryJobGivesAnEndedSubscriptionAReadOnlyGraceThenExpiresIt`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anEndedSubscriptionWithAutoRenewOffIsReadOnlyForSevenDaysThenExpires`

Not covered by an automated test: the in-app notices "Subscription ended: read-only" and "Subscription expired" to an individual owner, the audit entries `SUBSCRIPTION_GRACE_STARTED` and `SUBSCRIPTION_EXPIRED`, and the case where the job was down for more than 7 days (the subscription expires directly).

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
