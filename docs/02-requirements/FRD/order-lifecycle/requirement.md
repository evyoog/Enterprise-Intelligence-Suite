# REQ-ORD-001 — Order Lifecycle

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C39](../../../01-business/roadmap/open-decisions.md#c39))

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

## Actors
- Organization member (any `OrgRole`) — submits/cancels their own orders
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

## Out of scope
- Individual-customer orders (already served by `SubscriptionService#subscribe`, sprint 2026.4.3)
- Managing (configuring/suspending/deprovisioning) an org subscription once an order has provisioned it — the same organization-subscription-actions gap carried by [C38](../../../01-business/roadmap/open-decisions.md#c38)
- Multi-level approval routing or escalation — single ORG_ADMIN approval only
- Any generic, admin-configurable workflow engine (09.03) — see C39
- Any pricing/billing calculation — price is read from the product/plan, never charged (no payment integration exists anywhere in this platform)

## Dependencies
- Existing `Organization`, `OrganizationMember`, `Product`, `ProductPlan`.
- `SubscriptionService#subscribeOrganization` (new this sprint — the organization-owned equivalent of `#subscribe`).
- New table `orders` ([V009](../../../../database/migrations/V009__order_lifecycle_knowledge_base.sql)).
- New permission `MANAGE_ORDERS` (ORG_ADMIN).
