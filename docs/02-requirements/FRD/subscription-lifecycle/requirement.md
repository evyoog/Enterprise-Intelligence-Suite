# REQ-SUB-001 — Subscription Lifecycle

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-27 ([C38](../../../01-business/roadmap/open-decisions.md#c38)). Amended 2026-10-13: scheduled plan change (07.01.02.03 Schedule change), [C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-SUB-001 |
| Application | [07 Subscription & Entitlement Management](../../../01-business/roadmap/applications/07-subscription-entitlement-management.md) |
| Application code | `APP-SUB` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 07.01.01 | Suspend subscription; Upgrade; Downgrade; Renew; Cancel | [07 Subscription & Entitlement Management](../../../01-business/roadmap/applications/07-subscription-entitlement-management.md#0701-subscription-management) |
| 07.01.02 | Change plan | same |
| 07.01.02.03 | Schedule change (a downgrade takes effect at the next renewal) — [C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1 | same |
| 07.04.01 | Process renewal | same |
| 07.04.02 | Expire subscription; Reactivate subscription | same |

Create subscription and Activate subscription (07.01.01) already exist (`SubscriptionService#subscribe`, sprint before this one — see that method's own javadoc). 07.01.02 Change quantity (built in [REQ-SUB-003](../subscription-seats/requirement.md)), 07.02 Entitlement Management (built in [REQ-SUB-002](../entitlements/requirement.md)), 07.03 License & Quota Management, and 07.04.01 Schedule renewal/Notify renewal/Auto-renew are **not** covered by this requirement — see [C38](../../../01-business/roadmap/open-decisions.md#c38) for why each is carried.

## Summary
An individual customer can suspend, reactivate, cancel, renew, and change the plan on their own product subscriptions. A scheduled job automatically expires any active subscription past its `expiresAt`. No payment integration exists anywhere in this platform yet, so renewal only extends the term — there is nothing to charge.

**Plan changes ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1):** an **upgrade** takes effect **immediately**; a **downgrade** (a plan that costs less per month) is **scheduled** and takes effect **at the next renewal** — automatic or manual — so the customer keeps what they paid for until then. The customer can withdraw a scheduled downgrade before the renewal.

## Actors
- Individual customer (any authenticated Vyoog customer, acting on their own subscription only — see BR-SUB-004)
- The platform itself (the scheduled expiry job)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-001.1 | A customer can suspend their own ACTIVE subscription. | Must |
| REQ-SUB-001.2 | A customer can reactivate their own SUSPENDED subscription. | Must |
| REQ-SUB-001.3 | A customer can cancel their own subscription (except an already-CANCELLED one); cancellation is one-way. | Must |
| REQ-SUB-001.4 | A customer can renew their own subscription (not CANCELLED), extending `expiresAt` by its plan's billing period; renewing an EXPIRED subscription reactivates it. | Must |
| REQ-SUB-001.5 | A customer can change the plan on their own subscription (not CANCELLED) to another plan of the same product, or clear it back to the product's flat price. An upgrade (or a plan of the same monthly price) takes effect immediately; a downgrade is scheduled (REQ-SUB-001.8). | Must |
| REQ-SUB-001.6 | A scheduled job flips any ACTIVE subscription whose `expiresAt` has passed to EXPIRED. | Must |
| REQ-SUB-001.7 | Every action in this feature is refused with a generic "not found" for a subscription that does not belong to the caller — never confirming another customer's subscription exists. | Must |
| REQ-SUB-001.8 | **Scheduled downgrade** ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1): when the new plan is cheaper per month than the current one (a yearly price is divided by 12), the plan change is **scheduled**, not applied. The subscription keeps its current plan and shows the pending plan (`pendingPlanId`, `pendingPlanName`). | Must |
| REQ-SUB-001.9 | **Applied at renewal:** the scheduled plan becomes the subscription's plan when it next renews, whether by the auto-renew job or by the customer renewing manually, and the pending plan is cleared. The renewal (and its invoice) is at the new plan. | Must |
| REQ-SUB-001.10 | **Withdraw:** the customer can withdraw a scheduled downgrade before the renewal and stay on the current plan. Choosing an upgrade, or the plan already in force, also clears a scheduled downgrade. | Must |
| REQ-SUB-001.11 | An organization subscription's plan change follows the same rule, through the organization endpoints and the `MANAGE_SUBSCRIPTIONS` permission ([REQ-SUB-003](../subscription-seats/requirement.md), [C65](../../../01-business/roadmap/open-decisions.md#c65)). | Must |

## Out of scope
- Organization-owned subscriptions: no self-service action existed when this FRD was approved ([C38](../../../01-business/roadmap/open-decisions.md#c38)); organization suspend, cancel, seats and plan change were added later under [REQ-SUB-003](../subscription-seats/requirement.md)
- Proration — a plan change itself never charges or credits money. (A renewal now produces an invoice at the plan then in force; see [REQ-SUB-004](../renewal-reminders/requirement.md) and [REQ-BIL-001](../billing-payments/requirement.md).)
- Quantity/seat changes on a subscription ([REQ-SUB-003](../subscription-seats/requirement.md)). Scheduling a plan change for a date the customer picks: only "next renewal" exists ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1)
- Scheduling an **upgrade**: an upgrade is always immediate. Prorated charges or credits for the difference are not built (Not specified)
- Entitlement/license/quota tracking beyond the existing per-member product access model

## Dependencies
- Existing `ProductSubscription`, `ProductPlan`, `SubscriptionService#subscribe`.
- New column `product_subscription.plan_id` ([V008](../../../../database/migrations/V008__subscription_lifecycle.sql)).
- New status `SubscriptionStatus.SUSPENDED`.
- New columns `product_subscription.pending_plan_id` and `pending_plan_requested_at` ([V037](../../../../database/migrations/V037__billing_rules_sprint_2026_4_3.sql), [data model](../../../07-database/data-model/subscription-seats.md#scheduled-plan-change-columns)).

## Where SC-1 is built
| Part | Location |
|---|---|
| Service | `backend/src/main/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionService.java` — `applyPlanChange`, `isDowngrade`, `applyPendingPlan` (called from `autoRenew` and `renewSubscription`), `cancelScheduledPlanChange` |
| Controllers | `SubscriptionController` (`DELETE /me/subscriptions/{id}/plan/scheduled`), `OrganizationController` (`DELETE /organization/me/subscriptions/{id}/plan/scheduled`) |
| DTO | `SubscriptionDto.pendingPlanId`, `pendingPlanName` |
| Frontend | `frontend/src/pages/MySubscriptionsPage.tsx` (notice, Keep current plan, change-plan rule), `frontend/src/api/registrationApi.ts` (`cancelScheduledPlanChange`) |
| Tests | `ScheduledPlanChangeTest`; `MySubscriptionsPage.test.tsx`. Cases: [TC-SUB-038 to TC-SUB-044](../../../../test-cases/functional/subscription-lifecycle/) |

## Defaults applied (not asked)
Recorded because the product owner answered only "upgrade now, downgrade at renewal" ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1 B):
- **Upgrade or downgrade** is decided by price per month: a yearly plan's price divided by 12 (rounded half up to 4 decimals); a ONE_TIME price is taken as it is; a missing price counts as 0. A plan of the **same** monthly price is treated as an upgrade (immediate).
- **No current plan** (flat product price) to a plan, or a plan back to none: immediate.
- **Choosing the plan already in force** withdraws a scheduled downgrade instead of changing anything.
- **A second downgrade** replaces the first scheduled plan; only one change is pending at a time.
- **The scheduled change is dropped** if the pending plan no longer exists at renewal (the subscription keeps its current plan).
- A downgrade is allowed on any status except CANCELLED (the same rule as an immediate change); it takes effect at the next renewal, which for a SUSPENDED or EXPIRED subscription is when the customer renews it.
