# Acceptance criteria — Application access requests (REQ-TEN-010)

Functional test cases: [`test-cases/functional/application-access/`](../../../../test-cases/functional/application-access/TESTPLAN-TEN-010.md) (TC-TEN-080 onward, built 2026-10-15).

| ID | Requirement | Criterion |
|---|---|---|
| AC-1 | .1, BR-AAC-001 | **Given** an organization with an ACTIVE subscription to product A **when** a member requests access to A **then** a PENDING request is created and every organization admin / *Manage product access* holder is notified. |
| AC-2 | .1, BR-AAC-001 | **Given** an organization with no ACTIVE subscription to product B **when** a member requests access to B **then** the request is refused (400) naming the product. |
| AC-3 | .1, BR-AAC-002 | **Given** a member who already has ACTIVE access to product A **when** they request access to A again **then** the request is refused (400 "You already have access to…"). |
| AC-4 | .2, BR-AAC-003 | **Given** a member with a PENDING request for product A **when** they request A again **then** the second request is refused (400, duplicate pending). |
| AC-5 | .5, BR-AAC-005 | **Given** a PENDING request **when** an admin approves it **then** the member's access to the product becomes ACTIVE (the same seat check as a direct grant applies) and the member is notified. |
| AC-6 | .6, BR-AAC-006 | **Given** a PENDING request **when** an admin rejects it with a blank note **then** it is refused (400); **when** rejected with a reason **then** the request is REJECTED with that `decisionNote` and the member is notified. |
| AC-7 | .7, BR-AAC-007 | **Given** an already-APPROVED or already-REJECTED request **when** it is approved or rejected again **then** it is refused (400, "already been decided"). |
| AC-8 | .3, BR-AAC-004 | **Given** a PENDING request **when** the requester cancels it **then** it becomes CANCELLED; **when** cancelled again **then** it is refused (400, only a pending request can be cancelled). |
| AC-9 | .9, BR-AAC-010 | **Given** a member with ACTIVE access to a product **when** they remove their own access **then** it becomes INACTIVE at once, the seat is released, and the organization's subscription to that product is unchanged; **when** they later want it back **then** they must submit a new request (no reinstatement). |
| AC-10 | .8, BR-AAC-009 | **Given** an access request belonging to organization A **when** an admin of organization B tries to approve, reject or list it **then** it is refused (403/404) without revealing details of organization A's request. |
| AC-11 | (out of scope), BR-AAC-011 | **Given** an organization's subscription to a product is cancelled, suspended or expired **then** every member's access to that product is revoked automatically, as already built (no change verified by this FRD). |
| AC-12 | .10, BR-AAC-012 | **Given** a purchase request (`Order`) still SUBMITTED **when** fewer than 3 days have passed **then** `remindPendingOrders()` sends no reminder; **when** 3 or more days have passed **then** exactly one reminder is sent to every ACTIVE org admin, and a second run sends no further reminder; the order's status never changes because of the reminder. |
| AC-13 | .11, BR-AAC-013 | **Given** any request, decision or self-removal **then** the audit log records it (`ACCESS_REQUESTED`, `ACCESS_REQUEST_APPROVED`, `ACCESS_REQUEST_REJECTED`, `ACCESS_SELF_REMOVED`) and the relevant party is notified; a failed notification never fails the underlying change. |
