# TC-SUB-018: Paid periodic subscriptions get a renewal date, auto-renew and renew with an invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-018 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-2](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A Monthly plan of 499 INR.

## Steps
1. Subscribe from the cart.
2. Move the renewal date into the past; run the expiry job, then the renewal job twice.

## Expected Result
Auto-renew ON and renewal date = start + 30 days. The expiry job leaves it ACTIVE; the renewal job extends the term by 30 days from the old date, issues one renewal invoice, publishes `SubscriptionRenewed`, audits `SUBSCRIPTION_AUTO_RENEWED`; the second run changes nothing. Automatic charging is off (`canChargeAutomatically` = false).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `aNewMonthlySubscriptionAutoRenewsAndHasARenewalDate`, `aDueSubscriptionIsRenewedWithARenewalInvoiceAndNotExpired`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `expiryJobFlipsOverdueActiveSubscriptionsOnly`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
