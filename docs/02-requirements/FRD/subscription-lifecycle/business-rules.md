# Business rules — Subscription Lifecycle

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-SUB-001 | Suspend requires the subscription to currently be ACTIVE; otherwise refused (400). | backend | REQ-SUB-001.1 |
| BR-SUB-002 | Reactivate requires the subscription to currently be SUSPENDED; otherwise refused (400). | backend | REQ-SUB-001.2 |
| BR-SUB-003 | Cancel refuses an already-CANCELLED subscription (400); every other status may be cancelled. Cancellation is one-way — no action in this feature can move a CANCELLED subscription anywhere else. | backend | REQ-SUB-001.3 |
| BR-SUB-004 | Every action resolves the subscription and confirms `ownerType == INDIVIDUAL` and `ownerCustomerId == caller`, or refuses with the same 404 a nonexistent id would give. | backend | REQ-SUB-001.7 |
| BR-SUB-005 | Renew refuses a CANCELLED subscription (400). Otherwise it extends `expiresAt`: from the later of "now" and the current `expiresAt` (so renewing early does not shorten an existing term), by the subscription's plan's billing period — 30 days for MONTHLY, 365 for YEARLY. A subscription with no plan, or a ONE_TIME plan, can only be renewed if it already carries an `expiresAt` (extended by the same 30-day default); otherwise renewal is refused (400) — there is nothing recurring to extend. | backend | REQ-SUB-001.4 |
| BR-SUB-006 | Renewing a subscription whose status is EXPIRED also sets it back to ACTIVE. Renewing any other status leaves the status alone. | backend | REQ-SUB-001.4 |
| BR-SUB-007 | Change plan refuses a CANCELLED subscription (400). A non-null `planId` must belong to the same product as the subscription (404 if the plan doesn't exist, 400 if it belongs to a different product). A null `planId` clears the plan back to the product's flat price. | backend | REQ-SUB-001.5 |
| BR-SUB-008 | The scheduled expiry job (hourly) selects every subscription with `status = ACTIVE` and `expiresAt` in the past, and sets each to EXPIRED. SUSPENDED/CANCELLED subscriptions are never touched by it (they are not ACTIVE). | backend | REQ-SUB-001.6 |
| BR-SUB-009 | Every successful mutation is recorded in the audit log (`SUBSCRIPTION_SUSPENDED`/`_REACTIVATED`/`_CANCELLED`/`_RENEWED`/`_PLAN_CHANGED`/`_EXPIRED`) and, when the subscription has an individual owner, triggers a notification to that customer. Organization-owned subscriptions (out of this feature's scope) are never notified by it. | backend | REQ-SUB-001.1–.6 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
