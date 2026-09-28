# Business rules — Order Lifecycle

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-ORD-001 | Submitting an order requires an ACTIVE membership in good standing (same check as every other organization self-service action — see `OrderService#resolveMembership`). Create/Validate/Price/Submit (09.01.01.01–.04) are one call: the product must be ACTIVE, and a given `planId` must belong to that product, or the request is refused (400/404). No separate draft state exists. | backend | REQ-ORD-001.1, .2 |
| BR-ORD-002 | Approving an order requires the `MANAGE_ORDERS` organization permission (seeded to ORG_ADMIN) and refuses (400) an order that is not currently SUBMITTED. Approval calls `SubscriptionService#subscribeOrganization` in the same transaction, then sets the order to APPROVED — there is no separate PROVISIONED status or async step. | backend | REQ-ORD-001.3 |
| BR-ORD-003 | Rejecting an order requires the same `MANAGE_ORDERS` permission and also refuses (400) an order that is not currently SUBMITTED. The optional note is stored on the order either way (approve or reject). | backend | REQ-ORD-001.4 |
| BR-ORD-004 | Only the order's own requester may cancel it, and only while it is still SUBMITTED (400 otherwise) — an already-decided order is a historical record, not cancellable. | backend | REQ-ORD-001.5 |
| BR-ORD-005 | Every action resolves the order and confirms it belongs to the caller's own organization, or refuses with the same 404 a nonexistent id would give. | backend | REQ-ORD-001.7 |
| BR-ORD-006 | Every decision (approve/reject) notifies the requester; every new submission notifies every ACTIVE ORG_ADMIN of the organization. Both are recorded in the audit log (`ORDER_SUBMITTED`/`_APPROVED`/`_REJECTED`/`_CANCELLED`). | backend | REQ-ORD-001.3, .4 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
