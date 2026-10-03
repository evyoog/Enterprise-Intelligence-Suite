# Business rules — Access management (REQ-TEN-005)

The cross-feature rule [BR-ACC-001 Effective access](../../../03-business-rules/BR-ACC-001-effective-access.md) defines how effective access is calculated and enforced. The rules below are specific to this feature.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | **Effective access = role defaults + individual overrides.** For each feature permission and each entitled product: an override (granted ON or OFF) wins; otherwise the member's role default applies. | backend | .1, .3, .10 |
| BR-2 | **Delegation limit:** a delegated administrator can grant only feature permissions they hold themselves (in their own effective access). Removing a permission they do not hold is also refused. | backend + UI | .6a |
| BR-3 | **No self-change:** nobody can change their own role, feature permissions or product access, whatever permissions they hold. This extends the existing self-escalation guard (privileged access). | backend + UI | .6c |
| BR-4 | **Admins are changed only by admins:** only an organization admin can change another organization admin's role or access. | backend + UI | .6b |
| BR-5 | **Last admin:** the organization's last ACTIVE organization admin cannot be demoted, suspended or removed (existing rule, REQ-IAM-002, REQ-TEN-002.4). | backend + UI | .6d |
| BR-6 | **Admins locked ON:** an organization admin's effective access always contains every organization feature permission; overrides that would remove one are refused, and the role defaults of *Organization admin* cannot turn one off. | backend + UI | .2, .6e |
| BR-7 | **Entitled products only:** product access can be granted only for products in the organization's ACTIVE subscriptions whose catalogue status is active. When a subscription ends, its product disappears from effective access (overrides are kept but have no effect). | backend | .5 |
| BR-8 | **Seats:** if product access consumes a seat (Open question 3), a grant needs a free seat on that product's subscription; otherwise it is refused with "No free seats on {product}". | backend + UI | .5 |
| BR-9 | **Role change keeps overrides:** joining or changing role recalculates effective access from the (new) role defaults; existing overrides are kept. | backend | .1 |
| BR-10 | **Reset:** "Reset to role default" deletes the member's override for that item (or all their overrides); it never changes the role defaults. | backend | .3 |
| BR-11 | **D16 default:** organization subscription actions (suspend, reactivate, cancel, change plan, change seats) require *Manage subscriptions*; the *Organization admin* role holds it; *Member* does not unless granted. | backend | .4 |
| BR-12 | **Bulk:** a bulk change applies each member separately under BR-2 to BR-8; members that fail a rule are skipped with the reason, the others are changed. | backend | .8 |
| BR-13 | **Audit:** each change writes one audit entry per member and item: actor, member, item, from → to, and the basis (role default, override, reset). Role-default changes write one entry with the role, item, from → to and the number of affected members. | backend | .9 |
