# Business rules — Product Recommendations

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-MKT-001 | `featured` is a plain boolean on `Product`, defaulting to false; set only through the existing admin product create/update form (`MANAGE_CATALOG`). | backend | REQ-MKT-001.1 |
| BR-MKT-002 | The featured list is every `Product` with `status = ACTIVE` and `featured = true` — no further ranking or limit. | backend | REQ-MKT-001.2 |
| BR-MKT-003 | The popular list sums `ProductUsage.launchCount` per product across every customer, orders by that sum descending, and takes the top 8; any product in that top 8 that is not currently ACTIVE is filtered out (its usage history doesn't un-happen, but a retired/inactive product is never recommended). | backend | REQ-MKT-001.3, .4 |
| BR-MKT-004 | A product with no `ProductUsage` rows at all never appears in the popular list (there is nothing to sum). | backend | REQ-MKT-001.4 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
