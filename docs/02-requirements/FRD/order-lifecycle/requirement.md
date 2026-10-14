# REQ-ORD-001 — Order Lifecycle

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C39](../../../01-business/roadmap/open-decisions.md#c39)); extended by [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-1 B and CA-2 (product owner, 2026-10-14): REQ-ORD-001.8 to .11. Built 2026-10-14, migration [V040](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql).

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) |
| Requirement ID | REQ-ORD-001 |
| Application | [09 Order & Provisioning Management](../../../01-business/roadmap/applications/09-order-provisioning-management.md) |
| Application code | `APP-ORD` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 09.01.01 | Create order; Validate order; Price order; Submit order; Cancel order; Track order | [09 Order & Provisioning Management](../../../01-business/roadmap/applications/09-order-provisioning-management.md#0901-order-management) |
| 09.02.01 | Provision service; Activate service | same |
| 09.04.01 | Create approval; Approve; Reject | same |

Create/Validate/Price/Submit (09.01.01.01–.04) are one action, `submitOrder` — see business-rules.md. Configure/Suspend/Deprovision service (09.02.01.02/.04/.05), Route approval/Escalate (09.04.01.02/.06), and all of 09.03 Workflow Orchestration are **not** covered by this requirement — see [C39](../../../01-business/roadmap/open-decisions.md#c39) for why each is carried or deliberately not planned.

## Summary
Organization purchasing, gated by an ORG_ADMIN's decision: any member of an organization can request a product (optionally with a plan) for their organization; an ORG_ADMIN must approve it before the organization's subscription is actually created. An individual customer's own self-serve subscribe flow (07.01) is untouched — this requirement is for organization purchases only.

[C100](../../../01-business/roadmap/open-decisions.md#c100) extends this: an order is now a **purchase request that can hold several products and the requester's reason** (a cart becomes one order), approving it provisions every product and issues **one invoice**, and an organization administrator or a member with **Manage billing** can **buy directly** without a request (the purchase is recorded as an order approved by the buyer).

## Actors
- Organization member (any `OrgRole`) — submits/cancels their own orders (purchase requests)
- Organization administrator or member with **Manage billing** (`MANAGE_ORGANIZATION_BILLING`) — buys directly from the cart, with no request
- Organization administrator (`MANAGE_ORDERS`) — approves/rejects orders for their own organization

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-ORD-001.1 | Any active member of an organization can submit an order for a product (and, optionally, one of its plans) on behalf of their organization. | Must |
| REQ-ORD-001.2 | A plan that does not belong to the ordered product is refused. | Must |
| REQ-ORD-001.3 | An ORG_ADMIN (`MANAGE_ORDERS`) can approve a SUBMITTED order for their own organization, which provisions (creates or reactivates) the organization's subscription to that product/plan in the same action. | Must |
| REQ-ORD-001.4 | An ORG_ADMIN can reject a SUBMITTED order, with an optional note; nothing is provisioned. | Must |
| REQ-ORD-001.5 | The requester can cancel their own order while it is still SUBMITTED; a decided order (APPROVED/REJECTED) cannot be cancelled. | Must |
| REQ-ORD-001.6 | A member can see their own submitted orders; an ORG_ADMIN can see every order pending their decision for their organization. | Must |
| REQ-ORD-001.7 | An order belonging to a different organization is refused with a generic "not found", never confirming it exists. | Must |

### Several products, a reason, one invoice, direct purchase ([C100](../../../01-business/roadmap/open-decisions.md#c100))
| ID | Requirement | Priority |
|---|---|---|
| REQ-ORD-001.8 | An order can hold **several products** (each with an optional plan), each product **at most once**; every product is checked as in REQ-ORD-001.1 and .2 (ACTIVE product, plan of that product, audience rule of REQ-CAT-005). An order made by the cart holds the whole cart (REQ-MKT-003.12). Orders made before this change keep their single product as one item. | Must |
| REQ-ORD-001.9 | A purchase request carries the requester's **reason** (up to 500 characters). The cart requires it; the order API accepts it optionally. Every organization administrator is notified of a new request with the requester, the products and the reason, and the reason is shown to the administrator who decides and to the requester. | Must |
| REQ-ORD-001.10 | **Approving** an order with several products provisions **every** product and creates **one invoice** with one line per product (REQ-BIL-001.2), in the same action; rejecting provisions nothing. | Must |
| REQ-ORD-001.11 | **Direct purchase.** An organization administrator, or a member with **Manage billing**, does not need approval ([C100](../../../01-business/roadmap/open-decisions.md#c100) CA-2): the purchase is recorded as an order **approved by the buyer** (note "Purchased directly"), every product is provisioned and one invoice is created, in one action. When the buyer is not an organization administrator the **organization administrators are notified**. A direct purchase is made only from the cart ([REQ-MKT-003.13](../cart-checkout/requirement.md)); there is no separate endpoint. | Must |

## Out of scope
- Individual-customer orders (already served by `SubscriptionService#subscribe`, sprint 2026.4.3)
- Managing (configuring/suspending/deprovisioning) an org subscription once an order has provisioned it — the same organization-subscription-actions gap carried by [C38](../../../01-business/roadmap/open-decisions.md#c38)
- Multi-level approval routing or escalation — single ORG_ADMIN approval only
- Any generic, admin-configurable workflow engine (09.03) — see C39
- Payment itself: approving an order (or a direct purchase) issues the invoice ([REQ-BIL-001](../billing-payments/requirement.md)); paying it is the checkout's job. A coupon is never applied to a purchase request; the buyer's coupon applies to a direct purchase ([REQ-BIL-001.35](../billing-payments/requirement.md))
- Changing seats: not an order ([REQ-SUB-003.11](../subscription-seats/requirement.md))

## Dependencies
- Existing `Organization`, `OrganizationMember`, `Product`, `ProductPlan`.
- `SubscriptionService#subscribeOrganization` (new this sprint — the organization-owned equivalent of `#subscribe`).
- New table `orders` ([V009](../../../../database/migrations/V009__order_lifecycle_knowledge_base.sql)); [V040](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql) adds `orders.request_reason` and the table `order_item` ([data model](../../../07-database/data-model/cart-checkout.md#orders-and-order_item-v040)).
- The cart ([REQ-MKT-003](../cart-checkout/requirement.md)) and invoices ([REQ-BIL-001](../billing-payments/requirement.md)).
- New permission `MANAGE_ORDERS` (ORG_ADMIN).

## Open points
1. **The Orders page request form** (`/organization/orders`) still asks for one product and an optional plan and has **no reason field**; only the cart asks for the reason and holds several products. The page does list every product and the reason of an existing request. Decide whether the form should change.
2. **Direct purchase has no order endpoint** by design; if the product owner wants an organization administrator to buy without the cart, that is new scope.
