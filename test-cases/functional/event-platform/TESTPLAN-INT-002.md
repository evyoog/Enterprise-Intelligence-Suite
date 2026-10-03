# TESTPLAN-INT-002: Platform events

| Field | Value |
|---|---|
| Feature ID (required) | FTR-INT-002 |
| Requirement(s) covered | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) |
| Decision | [C62](../../../docs/01-business/roadmap/open-decisions.md#c62) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request with the engineering defaults recorded in the FRD) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md): TC-INT-011 to TC-INT-017.

## Approach
Backend: `OutboxDispatcherTest` (handlers registered by the test), `SubscriptionServiceTest` (lifecycle events), `SubscriptionSeatServiceTest` (SeatsChanged), `RenewalAndRemindersTest` (SubscriptionRenewed, RenewalReminderSent) and `PlatformEventsAuthorizationTest`, on the H2-backed Spring context with the scheduled dispatcher off. Frontend: `AdminPlatformEventsPage.test.tsx` (Vitest, Testing Library, jest-axe).

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-INT-011](TC-INT-011.md) | An event is written with its business change, and only if the change commits | AC-1, AC-2, AC-3 | Yes |
| [TC-INT-012](TC-INT-012.md) | A due event is delivered once to each handler; an event with no handler is delivered | AC-4 | Yes |
| [TC-INT-013](TC-INT-013.md) | A failing handler is retried with a doubling delay and the event fails after the maximum attempts | AC-5 | Yes |
| [TC-INT-014](TC-INT-014.md) | Events of one aggregate are delivered in order | AC-6 | Yes |
| [TC-INT-015](TC-INT-015.md) | Administrators list, filter, open and retry events | AC-7, AC-8, AC-11 | Yes |
| [TC-INT-016](TC-INT-016.md) | Delivered events older than the retention period are removed | AC-9 | Yes |
| [TC-INT-017](TC-INT-017.md) | Business changes publish their catalogue events | AC-10 | Partly |

## Blocked or partial
- Replay of delivered events (13.03.01.05) is Not specified and not tested.
- Order, invoice, payment and checkout events are published by code paths covered by their own tests; their event rows are not asserted individually (TC-INT-017 lists which are).
