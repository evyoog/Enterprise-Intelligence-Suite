# TESTPLAN-SUB-004: Auto-renewal and renewal reminders

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-004 |
| Requirement(s) covered | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) |
| Decision | [C64](../../../docs/01-business/roadmap/open-decisions.md#c64), [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) (RR-1 to RR-7) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request, extended 2026-10-14 for C100 with the engineering defaults recorded in the FRD) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md): TC-SUB-018 to TC-SUB-023 (C64) and TC-SUB-051 to TC-SUB-057 (C100: auto-renew off, the 7-day read-only grace period, grace reminders, reminder recipients).

## Approach
Backend: `RenewalAndRemindersTest` drives `RenewalService` and `RenewalReminderService.sendDue(now)` with fixed instants in several time zones; `RenewalRemindersAuthorizationTest`; `SubscriptionServiceTest` (the expiry job: grace period, then expiry, only without auto-renew); `SeatsGraceAndRecipientsTest` (grace period, grace reminders in the recipient's zone, recipients and channels); `CouponAuthorizationTest` (sign-in on the new endpoints). The 15-minute `RenewalJob` is off in tests. Frontend: `RenewalRemindersCard.test.tsx`, `RenewalReminderDefaultsPanel.test.tsx`, `MySubscriptionsPage.test.tsx`, `OrganizationSubscriptionsSection.test.tsx` and `RenewalRecipientsSection.test.tsx` with jest-axe.

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-SUB-018](TC-SUB-018.md) | Paid periodic subscriptions get a renewal date, auto-renew and renew with an invoice | AC-1, AC-2 | Yes |
| [TC-SUB-019](TC-SUB-019.md) | Daily reminders at the send time in the recipient's time zone, once per day | AC-3, AC-4, AC-8, AC-11 | Partly |
| [TC-SUB-020](TC-SUB-020.md) | Users choose their own days and time within the allowed range | AC-5, AC-6, AC-10 | Yes |
| [TC-SUB-021](TC-SUB-021.md) | Turning reminders off stops emails, not renewal | AC-7 | Yes |
| [TC-SUB-022](TC-SUB-022.md) | Platform defaults, organization recipients and audit | AC-9, AC-12 | Partly |
| [TC-SUB-023](TC-SUB-023.md) | Renewal screens are accessible and show the next reminder | AC-13 | Yes |
| [TC-SUB-051](TC-SUB-051.md) | Auto-renew can be turned off and on, with a confirmation | AC-14 | Partly |
| [TC-SUB-052](TC-SUB-052.md) | A term that ends with auto-renew off is read-only for 7 days, then expires | AC-15 | Yes |
| [TC-SUB-053](TC-SUB-053.md) | Turning auto-renew back on in the grace period renews at once | AC-16 | Yes |
| [TC-SUB-054](TC-SUB-054.md) | Grace reminders go out daily and stop at the end of the local grace day | AC-17 | Yes |
| [TC-SUB-055](TC-SUB-055.md) | An administrator chooses who else gets the reminders, on which channels | AC-18 | Yes |
| [TC-SUB-056](TC-SUB-056.md) | People with Manage billing are always-on recipients | AC-19 | Partly |
| [TC-SUB-057](TC-SUB-057.md) | The switch, the grace notice and the recipients section are accessible | AC-20 | Yes |

## Blocked or partial
- Automatic charging of a saved method is not possible yet (no Razorpay token integration); every renewal issues an invoice.
- Email wording (AC-11) and the admin default change (AC-9) are checked manually; the SMTP server is not reachable in tests.
- C100: the audit entry and event of the auto-renew switch, the organization permission check of the switch, the grace notices to an individual owner, the grace reminder's email wording, the Manage billing default recipient on the backend (TC-SUB-056) and the added person's own settings are **not** covered by an automated test (Not Run). An organization has no manual renew, so its grace period is left only by turning auto-renew on.
