# Acceptance criteria — Auto-renewal and renewal reminders (REQ-SUB-004)

| ID | Requirement | Criterion | Test case |
|---|---|---|---|
| AC-1 | .1 | **Given** a new subscription on a Monthly plan **then** auto-renew is ON and the renewal date is 30 days after the start. | [TC-SUB-018](../../../../test-cases/functional/renewal-reminders/TC-SUB-018.md) |
| AC-2 | .2 | **Given** an auto-renewing subscription whose renewal date has passed **when** the renewal job runs **then** the term is extended by one period, one renewal invoice is issued, `SubscriptionRenewed` is published; running the job again does nothing more. | [TC-SUB-018](../../../../test-cases/functional/renewal-reminders/TC-SUB-018.md) |
| AC-3 | .6 | **Default schedule:** **given** platform defaults (7 days, 09:00) **then** 7 reminders are sent, on days 7 to 1 before renewal, each at 09:00 in the recipient's time zone. | [TC-SUB-019](../../../../test-cases/functional/renewal-reminders/TC-SUB-019.md) |
| AC-4 | .6 | **Stops on renewal:** **given** reminders sent on days 7, 6, 5 **when** the subscription is renewed on day 4 **then** no further reminders are sent for that renewal. | [TC-SUB-019](../../../../test-cases/functional/renewal-reminders/TC-SUB-019.md) |
| AC-5 | .8 | **User's own days:** **given** a user setting of 3 days **then** reminders are sent on days 3, 2 and 1 only. | [TC-SUB-020](../../../../test-cases/functional/renewal-reminders/TC-SUB-020.md) |
| AC-6 | .7, .8 | **User's own time:** **given** a user time of 18:00 and time zone Asia/Kolkata **then** no reminder is sent before 18:00 IST that day and one is sent after. | [TC-SUB-020](../../../../test-cases/functional/renewal-reminders/TC-SUB-020.md) |
| AC-7 | .10 | **Off:** **given** reminders turned off **then** none are sent, and the subscription still auto-renews on its date. | [TC-SUB-021](../../../../test-cases/functional/renewal-reminders/TC-SUB-021.md) |
| AC-8 | .7 | **No duplicates:** **given** the job runs twice in one day **then** one email is sent and one log row exists. | [TC-SUB-019](../../../../test-cases/functional/renewal-reminders/TC-SUB-019.md) |
| AC-9 | .4 | **Admin default change:** **given** user A with their own days and user B without **when** the administrator changes the default from 7 to 5 **then** B follows 5 and A keeps their own. | [TC-SUB-022](../../../../test-cases/functional/renewal-reminders/TC-SUB-022.md) |
| AC-10 | .8 | **Given** days before 0 or 31, or a time "25:00" **then** the save is refused with a field error. | [TC-SUB-020](../../../../test-cases/functional/renewal-reminders/TC-SUB-020.md) |
| AC-11 | .9 | **Given** a sent reminder **then** its email contains the product, plan, renewal date, amount, "an invoice will be issued", and links to manage the subscription, pay, and change reminders. | [TC-SUB-019](../../../../test-cases/functional/renewal-reminders/TC-SUB-019.md) |
| AC-12 | .11 | **Given** a reminder is sent or a setting changes **then** an audit entry exists (and `RenewalReminderSent` for a sent reminder). | [TC-SUB-022](../../../../test-cases/functional/renewal-reminders/TC-SUB-022.md) |
| AC-13 | Accessibility | **Given** the Renewal reminders card, the subscription renewal details and the admin Renewal reminders tab **then** each passes the axe test. | [TC-SUB-023](../../../../test-cases/functional/renewal-reminders/TC-SUB-023.md) |
