# TESTPLAN-SUB-003: Seats and quantity

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SUB-003 |
| Requirement(s) covered | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) |
| Decision | [C63](../../../docs/01-business/roadmap/open-decisions.md#c63) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-03 at the product owner's request with the engineering defaults recorded in the FRD) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md): TC-SUB-013 to TC-SUB-017.

## Approach
Backend: `SubscriptionSeatServiceTest` on the H2-backed Spring context. Frontend: `OrganizationSubscriptionsSection.test.tsx` and `MySubscriptionsPage.test.tsx` with jest-axe.

## Test cases
| Test case | Title | Acceptance criteria | Automated |
|---|---|---|---|
| [TC-SUB-013](TC-SUB-013.md) | Organization subscriptions show seats; individual subscriptions have none | AC-1 | Yes |
| [TC-SUB-014](TC-SUB-014.md) | Increasing seats takes effect at once and is audited and published | AC-2, AC-5 | Yes |
| [TC-SUB-015](TC-SUB-015.md) | Seats cannot go below the seats in use | AC-3 | Yes |
| [TC-SUB-016](TC-SUB-016.md) | Adding a member beyond a subscription's seats is refused | AC-4 | Yes |
| [TC-SUB-017](TC-SUB-017.md) | Only organization managers of the same organization change seats; screens are accessible | AC-6, AC-7 | Yes |

## Blocked or partial
- Seat billing (charges or credits for a change) is Not specified (Open question 1) and not tested.
