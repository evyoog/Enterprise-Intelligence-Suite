# REQ-CAT-001 — Product Lifecycle & Structure

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-26 ([C32](../../../01-business/roadmap/open-decisions.md#c32))

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-CAT-001 |
| Application | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md) |
| Application code | `APP-CAT` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 02.01.01.03 | Version product | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md#feature-020101-product-lifecycle) |
| 02.01.01.04 | Publish product | same |
| 02.01.01.05 | Retire product | same |
| 02.01.02.01 | Define product hierarchy | [same page](../../../01-business/roadmap/applications/02-product-catalog-management.md#feature-020102-product-structure) |
| 02.01.02.02 | Define variants | same |
| 02.01.02.03 | Define dependencies | same |

02.01.01.01 Create product and 02.01.01.02 Update product are **not** in this FRD — both were already built in an earlier sprint (`ProductController#createProduct`/`#updateProduct`).

## Summary
Every product gains a plain revision counter shown on the admin edit page, and two dedicated lifecycle actions — Publish and Retire — alongside the existing Active/Inactive toggle. Retiring a product pulls it off the public storefront and blocks new subscriptions without deleting it or touching existing subscriptions/access; publishing again reverses it. Products can also be organized into a simple parent/variant hierarchy and can list other products they depend on.

## Actors
- Platform administrator (holding `MANAGE_CATALOG`, the existing gate on product mutation endpoints)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-CAT-001.1 | Every product update after creation increments a visible, read-only version counter. | P0 |
| REQ-CAT-001.2 | An administrator can publish a product (sets it ACTIVE), regardless of its current status. | P0 |
| REQ-CAT-001.3 | An administrator can retire a product (sets it RETIRED). A retired product does not appear in the public listing/search and cannot be newly subscribed to; existing subscriptions, product access, favorites and usage history are untouched. | P0 |
| REQ-CAT-001.4 | A retired product can be published again. | P0 |
| REQ-CAT-001.5 | An administrator can set a product's parent product and, when it has one, a variant label. | P0 |
| REQ-CAT-001.6 | An administrator can set which other products a product depends on. | P0 |
| REQ-CAT-001.7 | A product that is another product's parent, or that another product depends on, cannot be deleted (same "deactivate instead" refusal pattern as an existing subscription/access guard). | P0 |
| REQ-CAT-001.8 | A product cannot list itself as its own dependency. | P0 |

## Out of scope
- A full content-versioning history (past revisions are not stored, only a counter) — C32
- Enforcing a dependency at subscribe/order time (belongs to 09 Orders, a later sprint) — C32
- Multi-level hierarchy validation (cycles beyond direct self-parenting are not checked)
- Offering-level bundling (02.02 Offering Management — not this sprint)

## Dependencies
- Existing `ProductService`, `ProductController`, `ProductStatus`.
- New columns/table: `products.version`, `products.parent_product_id`, `products.variant_label`, `product_dependencies` ([V006](../../../../database/migrations/V006__product_structure_plan_pricing_member_lifecycle_groups.sql)).
