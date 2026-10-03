# TC-INT-017: Business changes publish their catalogue events

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-017 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A customer, an organization and paid and free plans.

## Steps
1. Subscribe, suspend, reactivate and cancel a subscription.
2. Change an organization subscription's seats.
3. Auto-renew a subscription; send a renewal reminder.
4. Approve an order; generate an invoice; capture a payment; complete a cart checkout (manual check of the Platform events screen).

## Expected Result
`SubscriptionCreated`, `SubscriptionSuspended`, `SubscriptionResumed`, `SubscriptionCancelled`, `SeatsChanged`, `SubscriptionRenewed` and `RenewalReminderSent` exist for the subscription aggregate with its ID. `OrderApproved`, `InvoiceGenerated`, `PaymentAuthorized`/`PaymentFailed` and `CheckoutCompleted` appear for their aggregates. No payload carries card data.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `lifecycleChangesPublishPlatformEvents`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `anAdminSeesSeatsAndCanIncreaseThemAtOnce`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `aDueSubscriptionIsRenewedWithARenewalInvoiceAndNotExpired`, `aReminderIsSentOncePerDayAtTheSendTimeInTheRecipientsTimeZone`

## Actual Result
The automated tests passed on 2026-10-03. The manual steps have not been run yet.

## Status
Automated part passed (2026-10-03); manual part not yet run

## Linked Defect (if failed)
None.
