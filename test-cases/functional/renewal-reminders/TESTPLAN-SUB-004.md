# TESTPLAN-SUB-004: Auto-renewal and renewal reminders

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-004 |
| Requirement(s) covered | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) |
| Decision | [C64](../../../docs/01-business/roadmap/open-decisions.md#c64) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request with the engineering defaults recorded in the FRD) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md): TC-SUB-018 to TC-SUB-023.

## Approach
Backend: `RenewalAndRemindersTest` drives `RenewalService` and `RenewalReminderService.sendDue(now)` with fixed instants in several time zones; `RenewalRemindersAuthorizationTest`; `SubscriptionServiceTest` (expiry only without auto-renew). The 15-minute `RenewalJob` is off in tests. Frontend: `RenewalRemindersCard.test.tsx`, `RenewalReminderDefaultsPanel.test.tsx`, `MySubscriptionsPage.test.tsx` with jest-axe.

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-SUB-018](TC-SUB-018.md) | Paid periodic subscriptions get a renewal date, auto-renew and renew with an invoice | AC-1, AC-2 | Yes |
| [TC-SUB-019](TC-SUB-019.md) | Daily reminders at the send time in the recipient's time zone, once per day | AC-3, AC-4, AC-8, AC-11 | Partly |
| [TC-SUB-020](TC-SUB-020.md) | Users choose their own days and time within the allowed range | AC-5, AC-6, AC-10 | Yes |
| [TC-SUB-021](TC-SUB-021.md) | Turning reminders off stops emails, not renewal | AC-7 | Yes |
| [TC-SUB-022](TC-SUB-022.md) | Platform defaults, organization recipients and audit | AC-9, AC-12 | Partly |
| [TC-SUB-023](TC-SUB-023.md) | Renewal screens are accessible and show the next reminder | AC-13 | Yes |

## Blocked or partial
- Automatic charging of a saved method is not possible yet (no Razorpay token integration); every renewal issues an invoice.
- Email wording (AC-11) and the admin default change (AC-9) are checked manually; the SMTP server is not reachable in tests.
