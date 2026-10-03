# Business rules — Seats and quantity (REQ-SUB-003)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | Quantity is an integer from 1 to 100 000. Individual subscriptions are always 1; a seat change on one is refused (400). | backend | .1 |
| BR-2 | A seat change takes effect at once; there is no pending or scheduled state. | backend | .2 |
| BR-3 | New quantity ≥ seats in use (ACTIVE members of the organization); otherwise 409 "{inUse} seats are in use. Remove members before reducing seats." | backend | .3 |
| BR-4 | Adding an ACTIVE member needs a free seat under the organization's licensed seats and under the quantity of each ACTIVE subscription of the organization, unless the organization allows seat overage. | backend | .4 |
| BR-5 | Increasing above the organization's licensed seats raises them to the new quantity; decreasing leaves them unchanged. | backend | .4 (default) |
| BR-6 | Only ACTIVE or SUSPENDED organization subscriptions of the caller's own organization can be changed; another organization's subscription is 404. | backend | .2 |
| BR-7 | Each change writes the audit entry `SUBSCRIPTION_SEATS_CHANGED` (from → to) and publishes `SeatsChanged` in the same transaction. | backend | .5 |
