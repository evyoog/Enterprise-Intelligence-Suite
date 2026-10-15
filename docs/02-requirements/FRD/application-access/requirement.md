# REQ-TEN-010 — Application access requests

**Status:** Approved (2026-10-15, [C103](../../../01-business/roadmap/open-decisions.md#c103), product owner revisit of WF-1/AP-1)
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-15
**Built:** 2026-10-15. Migration [V043](../../../../database/migrations/V043__access_requests_order_reminders.sql) (apply by hand, after V042). Functional test cases: [`test-cases/functional/application-access/`](../../../../test-cases/functional/application-access/TESTPLAN-TEN-010.md) (TC-TEN-080 onward).

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (AP-1 revisit) |
| Requirement ID | REQ-TEN-010 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` |
| Priority | P0 |
| AI required | No |

## Source functions
This FRD closes the **AP-1** gap found when the product owner's "Application Subscription and User Access Management" note was checked against the codebase ([C103](../../../01-business/roadmap/open-decisions.md#c103)). It does not touch organization purchasing (that is [REQ-ORD-001](../order-lifecycle/requirement.md)) or the admin-driven per-member access screen (that is [REQ-TEN-005](../access-management/requirement.md)); it layers a self-service request/approve-once flow, and a self-service removal, on top of REQ-TEN-005's `OrganizationProductAccess` record.

| Function ID | Function | Covered here |
|---|---|---|
| 09.04.01 (member-initiated slice) | Create approval; Approve; Reject | Yes, for one record type (`AccessRequest`), single-level only — see [C103](../../../01-business/roadmap/open-decisions.md#c103) for why no second level is built |

## Summary
A member of an organization can ask for their own access to a product the organization already subscribes to, when they do not already have it (self-service **access request**). An organization administrator, or anyone holding **Manage product access** ([REQ-TEN-005](../access-management/requirement.md)), approves (which grants access through the same path an administrator's own one-by-one grant uses) or rejects it with a reason. A member can also drop their own access to a product immediately, without an administrator and without touching the organization's subscription. Separately, an organization's purchase request (`Order`, [REQ-ORD-001](../order-lifecycle/requirement.md)) still `SUBMITTED` after 3 days gets one reminder to the organization's administrators — it is never auto-approved or auto-rejected.

This FRD is a close companion to [access-management](../access-management/requirement.md) (REQ-TEN-005), not a duplicate of it: REQ-TEN-005 is the **administrator-driven** screen that grants, revokes and overrides any member's access directly; REQ-TEN-010 is the **member-initiated** request a member raises for themself, decided once by an administrator, plus the member's own "remove my access" action.

## Actors
- **Organization member** (any `OrgRole`, ACTIVE membership): requests access to a product their organization subscribes to; views and cancels their own pending requests; removes their own access to a product at any time.
- **Organization administrator**, or a member holding **Manage product access** (`MANAGE_PRODUCT_ACCESS`, [REQ-TEN-005](../access-management/requirement.md)): sees every access request for the organization; approves (grants access, subject to the seat limit) or rejects (with a required reason).
- **Organization administrator** (`MANAGE_ORDERS`): receives the one-time 3-day reminder for a purchase request still awaiting their decision.

## Functional requirements
| ID | Requirement | Priority |
|----|-------------|----------|
| REQ-TEN-010.1 | An active member can request personal access to a product their organization has an ACTIVE subscription to, when they do not already have ACTIVE access to it, with an optional message. | Must |
| REQ-TEN-010.2 | A member cannot have more than one PENDING request for the same product at the same time. | Must |
| REQ-TEN-010.3 | A member can see their own requests ("My requests") and cancel one of their own requests while it is still PENDING. | Must |
| REQ-TEN-010.4 | An organization administrator, or a member holding *Manage product access*, can see every access request raised in their own organization. | Must |
| REQ-TEN-010.5 | Approving a PENDING request grants the member access to the product through the same mechanism an administrator's direct grant uses (the same seat check and audit trail), and notifies the member. | Must |
| REQ-TEN-010.6 | Rejecting a PENDING request requires a reason, records who rejected it and when, and notifies the member. | Must |
| REQ-TEN-010.7 | A request already APPROVED, REJECTED or CANCELLED cannot be decided again. | Must |
| REQ-TEN-010.8 | A decision (approve/reject), and a cancel, on a request belonging to a different organization is refused as if the request did not exist (404) or without permission (403), never confirming details of another organization's request. | Must |
| REQ-TEN-010.9 | A member can remove their own ACTIVE access to a product at any time, immediately (no approval needed); this does not change or cancel the organization's subscription to that product. Getting access back afterwards always needs a new request. | Must |
| REQ-TEN-010.10 | A purchase request (`Order`) still SUBMITTED 3 or more days after creation is reminded to the organization's administrators exactly once; it is never auto-approved or auto-rejected by this reminder. | Must |
| REQ-TEN-010.11 | Every request, decision and self-removal is recorded in the audit log, and every organization administrator (or *Manage product access* holder, for a new request) is notified. A failed notification never fails the underlying change. | Must |

## Out of scope
- Multiple approval levels or amount-based approval rules for access requests — single-level approval only, see [C103](../../../01-business/roadmap/open-decisions.md#c103) (the architecture stays extensible: `AccessRequestService.approve`/`reject` is the one place a second level would be added).
- A general, admin-configurable workflow engine (09.03) — confirmed not needed; see [C103](../../../01-business/roadmap/open-decisions.md#c103) WF-1 and [C39](../../../01-business/roadmap/open-decisions.md#c39).
- Requesting a new organization subscription to a product the organization does not already have — that is a purchase request ([REQ-ORD-001](../order-lifecycle/requirement.md)), not an access request.
- Revoking a member's access because an organization's subscription was cancelled, suspended or expired — already correct, unrelated event-driven behaviour (`SubscriptionService#afterOrganizationChange` → `OrganizationEntitlementsChangedEvent` → `AccessSyncService#onEntitlementsChanged`); nothing changed here.
- A "removal needs approval" policy — self-removal is deliberately immediate and unapproved; getting access back always needs a new request, which is approval enough.
- Auto-approving or auto-rejecting a pending order after its reminder, a scheduled/grace-period cancellation, and a platform-wide cross-organization list of requests — all out of scope, see [C103](../../../01-business/roadmap/open-decisions.md#c103).

## Dependencies
- [REQ-TEN-005](../access-management/requirement.md) Access management — `OrganizationProductAccess`, `AccessSyncService#grantProduct`/`revokeProduct`, the `MANAGE_PRODUCT_ACCESS` permission and its seat check.
- [REQ-ORD-001](../order-lifecycle/requirement.md) Order Lifecycle — `Order`/`OrderStatus`, extended with the 3-day reminder (`OrderService.remindPendingOrders`, `OrderReminderJob`).
- Product catalog (`Product`, `ProductStatus.ACTIVE`), organization membership (`OrganizationMember`, `MembershipStatus.ACTIVE`), notifications, audit log.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) |
| API | [api-requirements.md](api-requirements.md) → [application-access.md](../../../06-api/api-requirements/application-access.md) |
| Data model | [application-access.md](../../../07-database/data-model/application-access.md) |
| Test cases | [test-cases/functional/application-access](../../../../test-cases/functional/application-access/TESTPLAN-TEN-010.md) |
