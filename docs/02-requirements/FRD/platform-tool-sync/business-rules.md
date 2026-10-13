# Business rules — Platform ↔ tool synchronization

Cross-feature rules are in `docs/03-business-rules/`: [BR-INT-001](../../../03-business-rules/BR-INT-001-single-writer-and-tenant-scope.md) (single writer, tenant scope) and [BR-SUB-010](../../../03-business-rules/BR-SUB-010-subscription-end-time.md) (subscription end time).

| ID | Rule | Source |
|---|---|---|
| BR-SYN-001 | Centrally owned data (organization, hierarchy, users, memberships, subscriptions, product access) is changed only by the platform. A tool changes it only by asking the platform and applying the answer (BR-INT-001). | REQ-INT-003.8 |
| BR-SYN-002 | A message is applied only if its `version` is greater than the version held for that aggregate. Equal or lower versions are acknowledged as `duplicate` and change nothing. | .4 |
| BR-SYN-003 | A message is applied at most once per `eventId`; a repeat answers `duplicate`. Applying a message, recording its `eventId` and recording the new version happen in one database transaction. | .4, .5 |
| BR-SYN-004 | An event is delivered only to tools for which the organization has an ACTIVE or SUSPENDED subscription and a READY tenant. A tool the organization never subscribed to never receives that organization's data. | .3 |
| BR-SYN-005 | Delivery to one tool never waits for another tool. A tool that is down leaves its own deliveries pending or FAILED and affects no other. | .5 |
| BR-SYN-006 | A delivery counts as delivered only when the tool answered `applied` or `duplicate`. `retry` is retried with backoff; `rejected` is permanent and recorded. After the retry limit a delivery is FAILED. | .6 |
| BR-SYN-007 | A tenant reference in a message is resolved through the receiver's registry. A message never carries, and a receiver never accepts, a database name, schema name or connection detail. | .16 |
| BR-SYN-008 | Identity: users are keyed by Keycloak `sub`. Two people are never merged because their email matches. Email may link a local pre-existing row only when the Keycloak email is verified; the link is recorded (who, which row, which email, when). | .9 |
| BR-SYN-009 | A tool creating a Keycloak user: look the email up first and reuse the existing user; otherwise create with the real email; report to the platform with an idempotency key; if the report fails, disable the user just created. | .10 |
| BR-SYN-010 | Applying user, membership or access data never creates product business records. | .11 |
| BR-SYN-011 | Never published to any tool: passwords, tokens, MFA secrets, payment or bank data, invoices, internal notes. The organization snapshot carries all other organization fields. | .12 |
| BR-SYN-012 | A subscription is effective while `startsAt ≤ now ≤ endsAt`, where `endsAt` follows BR-SUB-010. After `endsAt` access is denied at once. A cancelled or suspended subscription denies access once the tool has applied the change (within 5 minutes at most; at once for sensitive actions). | .13, .15 |
| BR-SYN-013 | Product access exists only for products in the organization's ACTIVE subscriptions. A product role from the platform is mapped to a tool role by the tool's adapter; an unknown role maps to the tool's least-privileged role. | .14 |
| BR-SYN-014 | Sensitive actions are checked with the platform and fail closed. Which actions are sensitive is a list owned by each tool and reviewed with the product owner (user and role administration, hierarchy changes, data export, settings, approvals). | .15 |
| BR-SYN-015 | Provisioning is idempotent and safe to repeat. Expiry, suspension or cancellation never deletes a tenant schema or business data. | .17 |
| BR-SYN-016 | Hierarchy: a node's parent may arrive after the node; the node waits (pending parent) and is placed when the parent arrives. A move that would create a cycle is rejected. A node with children or placed users is not deleted by a tool; the rejection is recorded and reconcile reports it. | .2 |
| BR-SYN-017 | A write-through request carries an idempotency key. Repeating a request with the same key returns the first result and changes nothing more. | .8 |
| BR-SYN-018 | Replay is allowed only to users with `MANAGE_INTEGRATIONS`, is audited, and delivers the **current** state of the aggregate, not an old one. | .6, .21 |
