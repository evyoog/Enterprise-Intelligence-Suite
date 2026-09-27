# REQ-SUB-001 — Subscription Lifecycle

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-27 ([C38](../../../01-business/roadmap/open-decisions.md#c38))

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
| 07.04.01 | Process renewal | same |
| 07.04.02 | Expire subscription; Reactivate subscription | same |

Create subscription and Activate subscription (07.01.01) already exist (`SubscriptionService#subscribe`, sprint before this one — see that method's own javadoc). 07.01.02 Change quantity/Schedule change, 07.02 Entitlement Management, 07.03 License & Quota Management, and 07.04.01 Schedule renewal/Notify renewal/Auto-renew are **not** covered by this requirement — see [C38](../../../01-business/roadmap/open-decisions.md#c38) for why each is carried.

## Summary
An individual customer can suspend, reactivate, cancel, renew, and change the plan on their own product subscriptions. A scheduled job automatically expires any active subscription past its `expiresAt`. No payment integration exists anywhere in this platform yet, so renewal only extends the term — there is nothing to charge.

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
| REQ-SUB-001.5 | A customer can change the plan on their own subscription (not CANCELLED) to another plan of the same product, or clear it back to the product's flat price. | Must |
| REQ-SUB-001.6 | A scheduled job flips any ACTIVE subscription whose `expiresAt` has passed to EXPIRED. | Must |
| REQ-SUB-001.7 | Every action in this feature is refused with a generic "not found" for a subscription that does not belong to the caller — never confirming another customer's subscription exists. | Must |

## Out of scope
- Organization-owned subscriptions (no admin/self-service action exists yet for these — see [C38](../../../01-business/roadmap/open-decisions.md#c38))
- Any payment, invoicing, or proration — renewal/plan-change only ever adjusts `status`/`expiresAt`/`planId`, never money
- Quantity/seat changes on a subscription, or scheduling a change for a future date
- Entitlement/license/quota tracking beyond the existing per-member product access model

## Dependencies
- Existing `ProductSubscription`, `ProductPlan`, `SubscriptionService#subscribe`.
- New column `product_subscription.plan_id` ([V008](../../../../database/migrations/V008__subscription_lifecycle.sql)).
- New status `SubscriptionStatus.SUSPENDED`.
