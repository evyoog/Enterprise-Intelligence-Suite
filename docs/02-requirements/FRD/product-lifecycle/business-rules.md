# Business rules — Product Lifecycle & Structure

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-CAT-001 | Every `updateProduct` call increments `version` by 1; `createProduct` starts a product at version 1. | backend | REQ-CAT-001.1 |
| BR-CAT-002 | Publish always sets status to ACTIVE, from any prior status (INACTIVE or RETIRED). | backend | REQ-CAT-001.2 |
| BR-CAT-003 | Retire always sets status to RETIRED, from any prior status. | backend | REQ-CAT-001.3 |
| BR-CAT-004 | A RETIRED product is excluded from `GET /products` and `GET /products/search` (public, `includeInactive=false`) the same way INACTIVE already is, but still appears in the ADMIN listing/search. | backend | REQ-CAT-001.3 |
| BR-CAT-005 | `SubscriptionService#subscribe` only offers ACTIVE products — already true before this feature, and unchanged, so RETIRED is excluded from new subscriptions with no separate check. | backend | REQ-CAT-001.3 |
| BR-CAT-006 | Retiring or publishing a product never changes its subscriptions, organization product access, favorites or usage history. | backend | REQ-CAT-001.3 |
| BR-CAT-007 | A product cannot list itself in `dependsOnProductIds` — rejected with a 400. | backend | REQ-CAT-001.8 |
| BR-CAT-008 | Deleting a product is refused (409, same pattern as `CatalogProductUsageGuard`) while another product lists it as `parentProductId`, or lists it in `dependsOnProductIds`. | backend | REQ-CAT-001.7 |
| BR-CAT-009 | `variantLabel` is accepted with no parent, but is only meaningful (shown as a distinguishing label) when a parent is also set — this is a UI convention, not a backend validation. | frontend | REQ-CAT-001.5 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
