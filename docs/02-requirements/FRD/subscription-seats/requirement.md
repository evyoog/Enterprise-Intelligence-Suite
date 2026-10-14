# REQ-SUB-003 — Seats and quantity

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Every open question that blocked approval is answered ([C63](../../../01-business/roadmap/open-decisions.md#c63), [C90](../../../01-business/roadmap/open-decisions.md#c90), [C100](../../../01-business/roadmap/open-decisions.md#c100)); what remains is the product owner's confirmation of the engineering defaults, above all per-seat pricing (see [Open points](#open-points)).
**Decision:** [C63](../../../01-business/roadmap/open-decisions.md#c63) (answer to D14, option A), [C90](../../../01-business/roadmap/open-decisions.md#c90) (AM-3 B, named seats per product), [C100](../../../01-business/roadmap/open-decisions.md#c100) (SE-1 to SE-4, 2026-10-14)
**Built:** 2026-10-03 at the product owner's request with the engineering defaults of [C63](../../../01-business/roadmap/open-decisions.md#c63); **changed 2026-10-14 for [C100](../../../01-business/roadmap/open-decisions.md#c100)** (per-seat pricing, a seat increase charged now, a decrease at renewal, the Manage billing permission; migration [V040](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql)). Test cases: [TESTPLAN-SUB-003](../../../../test-cases/functional/subscription-seats/TESTPLAN-SUB-003.md).

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
| 07.01.02 | Change quantity | Yes: an increase now, a decrease at the next renewal (.2, .3, .7 to .12). A change on a chosen future date: No (later, D14) |
| 07.03.01 | Licensing | Yes, seats on organization subscriptions (.1, .4) |
| 07.03.02 | Usage limits | Only the seat limit (.4). Usage quotas are not covered ([C52](../../../01-business/roadmap/open-decisions.md#c52)) |

## Summary
An organization subscription has a **seat quantity**, and its plan price is **per seat**. Anyone with the organization **Manage billing** permission increases or decreases it, never below the seats in use. An **increase takes effect now and is charged now**, for the remaining days of the current term, on its own invoice. A **decrease takes effect at the next renewal, with no credit**, and can be withdrawn before then. Seat use and enforcement reuse the organization's existing seat-limit model ([C63](../../../01-business/roadmap/open-decisions.md#c63)) and the named seats per product of [C90](../../../01-business/roadmap/open-decisions.md#c90). Individual subscriptions always have quantity 1.

## Actors
- **Organization user with Manage billing** (`MANAGE_ORGANIZATION_BILLING`; organization administrators have it): views and changes seats ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-2 A).
- **Organization user with Manage subscriptions** (`MANAGE_SUBSCRIPTIONS`): can view seats, not change them.
- **Organization member**: uses a seat of a product while that product is ON for them ([C90](../../../01-business/roadmap/open-decisions.md#c90)).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-003.1 | An **organization subscription** has a **seat quantity** of at least 1. Individual subscriptions are always 1 and have no seat control. | Must |
| REQ-SUB-003.2 | An authorized organization user can **increase or decrease** seats. An **increase takes effect immediately** (.8); a **decrease takes effect at the next renewal** (.9) ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-1 B; replaces the C63 "immediate" rule for a decrease). | Must |
| REQ-SUB-003.3 | Seats cannot be reduced **below the seats in use**; the screen shows the seats in use and the refusal says why. | Must |
| REQ-SUB-003.4 | Seat use reuses the organization **seat-limit** model and its enforcement: adding a member beyond the limit is refused. | Must |
| REQ-SUB-003.5 | Every seat change is audited and publishes `SeatsChanged` ([REQ-INT-002](../event-platform/requirement.md)). A scheduled decrease and its withdrawal are audited too. | Must |
| REQ-SUB-003.6 | A seat change **on a date the user chooses** is out of scope (later, per D14). The only scheduled change is the decrease at renewal (.9). | — |
| REQ-SUB-003.7 | **Price per seat** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-1, engineering default to confirm): for an organization subscription the plan's price is the price of **one seat for one term**. A new subscription and every renewal invoice bill **price × seats**; the invoice line shows the quantity. An individual subscription is always one seat. | Must |
| REQ-SUB-003.8 | **An increase is charged now** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-1 B): the added seats are charged for the **remaining days of the current term** on **their own invoice** (line "Added seats: {product} — {plan}"): added seats × seat price × remaining days ÷ days in the term (30 for a Monthly plan, 365 for a Yearly plan), taxed like any invoice ([REQ-BIL-002](../tax-rules/requirement.md)). Nothing is invoiced for a free plan or when no days remain. | Must |
| REQ-SUB-003.9 | **A decrease waits for the next renewal, with no credit** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-1 B). It is stored as a **pending quantity**, shown on the screen with the renewal date, and can be **withdrawn** before then. When the subscription renews the quantity becomes the pending value and the renewal invoice bills the lower number. **Never below the seats in use at that moment:** if more seats are in use than were asked for, the renewal keeps the seats in use. A subscription with no renewal date has nothing to wait for, so a decrease applies at once. | Must |
| REQ-SUB-003.10 | **Who changes seats** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-2 A): the organization **Manage billing** permission. Manage subscriptions (and Manage billing) can **view** the organization's subscriptions and their seats; any other member is refused (403). | Must |
| REQ-SUB-003.11 | Seat changes **do not go through order approval** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-4 A). | Must |
| REQ-SUB-003.12 | Seats cannot be changed on a subscription whose **term has ended** (409 "This term has ended. Renew the subscription before changing seats."), including while it is read-only in its grace period ([REQ-SUB-004.13](../renewal-reminders/requirement.md)). | Must |

## Engineering defaults
| Topic | Default | Where |
|---|---|---|
| Seats in use (OQ 3) | **Named seats per product** ([C90](../../../01-business/roadmap/open-decisions.md#c90) AM-3 B): the ACTIVE members who have that product ON. (The first build counted every ACTIVE member as a pool.) | `ProductSeatService`, `SubscriptionSeatService` |
| Enforcement | Adding a member is refused when the active members reach the organization's licensed seats (unchanged) **or** the seat quantity of any of its ACTIVE subscriptions. Platform-admin "allow seat overage" still bypasses both | `OrganizationMemberService.assertSeatAvailable` |
| Organization seat limit | Increasing a subscription's seats above the organization's licensed seats raises the organization's licensed seats to match, so the purchase takes effect at once. Decreasing, and applying a decrease at renewal, never lower it (platform administrators set it) | `SubscriptionSeatService` |
| Initial quantity | A new organization subscription starts at the seats the organization already has on that product, else at the organization's licensed seats (at least the active members, at least 1) and is billed for them | `SubscriptionService.initialSeats` |
| Price per seat (**to confirm**) | The plan's price × seats, see REQ-SUB-003.7. It changes what organizations are invoiced | `InvoiceService.generate` |
| Remaining days | Rounded **up** to whole days, at most the days in the term; days in the term are 30 (Monthly), 365 (Yearly), otherwise the length of the current term (30 if unknown) | `SubscriptionSeatService.remainingDays`, `termDays` |
| Charge basis | Added seats × seat price × (remaining days ÷ days in the term), rounded to the currency's smallest unit; a coupon is not applied to a seat-increase invoice | `InvoiceService.generateSeatIncrease` |
| Withdraw | Asking for the current quantity again clears the pending decrease; asking for it with nothing pending changes nothing | `SubscriptionSeatService.changeSeats` |
| Who may change seats (OQ 2) | **Manage billing** ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-2 A), no longer `MANAGE_ORGANIZATION` | `SubscriptionSeatService` |
| Order approval (OQ 4) | Not required ([C100](../../../01-business/roadmap/open-decisions.md#c100) SE-4 A) | — |
| Maximum | 100 000 seats | `ChangeSeatsRequest` |

## Out of scope
A seat change on a date the user chooses; credit or refund for a decrease; seat-based discounts or tiers; usage quotas; price books ([C50](../../../01-business/roadmap/open-decisions.md#c50)).

## Dependencies
Organization members and seat limit (REQ-TEN-*), named seats per product (REQ-TEN-005), subscription lifecycle (REQ-SUB-001), renewals (REQ-SUB-004), invoices and tax (REQ-BIL-001, REQ-BIL-002), platform events (REQ-INT-002), audit.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [subscription-seats.md](../../../05-ui/screen-requirements/subscription-seats.md) |
| API | [api-requirements.md](api-requirements.md) → [subscription-seats.md](../../../06-api/api-requirements/subscription-seats.md) |
| Data model | [subscription-seats.md](../../../07-database/data-model/subscription-seats.md) (V018, V037, V040) |

## Open points
Raised for the product owner ([C100](../../../01-business/roadmap/open-decisions.md#c100) "Defaults applied (to confirm)"); none blocks use of the feature.
1. **Per-seat pricing is an engineering default to confirm.** C100 SE-1 says an increase is charged for the remaining days; it does not say what one seat costs. The build treats the plan price as per seat (REQ-SUB-003.7), so a new or renewal invoice for an organization is price × seats, where before it was one price. Existing organization subscriptions will be invoiced for more at their next renewal. Confirm, or tell us the alternative (a flat price per subscription).
2. **There is no organization "Renew now" endpoint.** An individual can renew from the grace banner (`POST /me/subscriptions/{id}/renew`); an organization turns auto-renew back on, which renews at once ([REQ-SUB-004.14](../renewal-reminders/requirement.md)). See [renewal-reminders](../renewal-reminders/requirement.md#open-points).
3. **The cart shows seats × price** for an organization purchase, so the cart total is the first invoice's amount ([cart-checkout](../cart-checkout/requirement.md#open-points)).

## Open questions
All answered. None remains open.

| # | Question | Answer |
|---|---|---|
| 1 | ~~**Billing of a mid-term change:** is an increase charged immediately (prorated) or from the next renewal? Is a decrease credited?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) SE-1 B.** An increase is charged now for the remaining days of the term; a decrease applies at the next renewal with no credit ([REQ-SUB-003.8, .9](#functional-requirements)). Per-seat pricing is the engineering default that makes this work ([Open points](#open-points)). |
| 2 | ~~**Who may change seats:** organization admins only, or a billing permission?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) SE-2 A.** Anyone with Manage billing; organization admins have it ([REQ-SUB-003.10](#functional-requirements)). |
| 3 | ~~**Seat assignment:** pool or named seats?~~ | **Answered — [C90](../../../01-business/roadmap/open-decisions.md#c90) (AM-3 B), confirmed by [C100](../../../01-business/roadmap/open-decisions.md#c100) SE-3 B:** named seats per product — a member uses a seat of a product only while it is ON for them (REQ-TEN-005). |
| 4 | ~~Do seat changes go through **order approval** (REQ-ORD-001)?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) SE-4 A.** No ([REQ-SUB-003.11](#functional-requirements)). |
