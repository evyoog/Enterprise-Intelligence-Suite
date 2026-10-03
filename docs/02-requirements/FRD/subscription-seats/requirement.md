# REQ-SUB-003 — Seats and quantity

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C63](../../../01-business/roadmap/open-decisions.md#c63) (answer to D14, option A)
**Built:** 2026-10-03 at the product owner's request, with the engineering defaults below. Test cases: [TESTPLAN-SUB-003](../../../../test-cases/functional/subscription-seats/TESTPLAN-SUB-003.md).

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-SUB-003 |
| Application | [07 Subscription & Entitlement Management](../../../01-business/roadmap/applications/07-subscription-entitlement-management.md) |
| Application code | `APP-SUB` |
| Priority | P0 |
| AI required | No |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 07.01.02 | Change quantity | Yes, immediate seat changes (.2, .3). Schedule change: No (later, D14) |
| 07.03.01 | Licensing | Yes, seats on organization subscriptions (.1, .4) |
| 07.03.02 | Usage limits | Only the seat limit (.4). Usage quotas are not covered ([C52](../../../01-business/roadmap/open-decisions.md#c52)) |

## Summary
An organization subscription has a **seat quantity**. Authorized organization users increase or decrease it with **immediate effect**, never below the seats in use. Seat use and enforcement reuse the organization's existing seat-limit model ([C63](../../../01-business/roadmap/open-decisions.md#c63)). Individual subscriptions always have quantity 1.

## Actors
- **Organization administrator** (`MANAGE_ORGANIZATION` — Open question 2): views and changes seats.
- **Organization member**: uses a seat.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-003.1 | An **organization subscription** has a **seat quantity** of at least 1. Individual subscriptions are always 1 and have no seat control. | Must |
| REQ-SUB-003.2 | An authorized organization user can **increase or decrease** seats; the change **takes effect immediately**. | Must |
| REQ-SUB-003.3 | Seats cannot be reduced **below the seats in use**; the screen shows the seats in use and the refusal says why. | Must |
| REQ-SUB-003.4 | Seat use reuses the organization **seat-limit** model and its enforcement: adding a member beyond the limit is refused. | Must |
| REQ-SUB-003.5 | Every seat change is audited and publishes `SeatsChanged` ([REQ-INT-002](../event-platform/requirement.md)). | Must |
| REQ-SUB-003.6 | **Scheduled** seat changes (future date or at renewal) are out of scope (later, per D14). | — |

## Engineering defaults (built 2026-10-03, until the open questions are answered)
| Topic | Default | Where |
|---|---|---|
| Seats in use (OQ 3) | A **pool**: every ACTIVE member of the organization uses one seat (the same count the existing seat limit uses) | `SubscriptionSeatService` |
| Enforcement | Adding a member is refused when the active members reach the organization's licensed seats (unchanged) **or** the seat quantity of any of its ACTIVE subscriptions. Platform-admin "allow seat overage" still bypasses both | `OrganizationMemberService.assertSeatAvailable` |
| Organization seat limit | Increasing a subscription's seats above the organization's licensed seats raises the organization's licensed seats to match, so the purchase takes effect at once. Decreasing never lowers it (platform administrators set it) | `SubscriptionSeatService` |
| Initial quantity | A new organization subscription starts at the organization's licensed seats (at least the active members, at least 1); existing organization subscriptions were set the same way by the migration | `SubscriptionService.subscribeOrganization`, `V018` |
| Who may change seats (OQ 2) | `MANAGE_ORGANIZATION` (as for organization billing, [C47](../../../01-business/roadmap/open-decisions.md#c47)) | `OrganizationSelfService.requireOrganizationManagement` |
| Billing (OQ 1) | **No charge or credit** for a mid-term change; invoices are unchanged | — |
| Order approval (OQ 4) | Not required | — |
| Maximum | 100 000 seats | `ChangeSeatsRequest` |

## Out of scope
Scheduled changes; seat-based pricing and proration; named seat assignment; usage quotas.

## Dependencies
Organization members and seat limit (REQ-TEN-*), subscription lifecycle (REQ-SUB-001), platform events (REQ-INT-002), audit.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [subscription-seats.md](../../../05-ui/screen-requirements/subscription-seats.md) |
| API | [api-requirements.md](api-requirements.md) → [subscription-seats.md](../../../06-api/api-requirements/subscription-seats.md) |
| Data model | [subscription-seats.md](../../../07-database/data-model/subscription-seats.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Billing of a mid-term change:** is an increase charged immediately (prorated) or from the next renewal? Is a decrease credited? Change plan has no proration today ([C38](../../../01-business/roadmap/open-decisions.md#c38)). | Yes |
| 2 | **Who may change seats:** organization admins only, or a billing permission (D16 not decided)? | No — confirm in review |
| 3 | **Seat assignment:** is a seat a pool used by any member, or assigned to named members? | No — confirm in review |
| 4 | Do seat changes on organization subscriptions go through **order approval** (REQ-ORD-001)? | No — confirm in review |
