# REQ-MKT-001 — Product Recommendations

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C40](../../../01-business/roadmap/open-decisions.md#c40))

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-MKT-001 |
| Application | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md) |
| Application code | `APP-MKT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) — see Out of scope |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 03.01.02 | Recommend products; Show featured products; Show popular products | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md#0301-product-discovery) |

03.01.01 Catalog Browsing (browse/search/filter/sort) is already satisfied by the existing public catalog search (`ProductController#searchProducts`, Phase 17) — not covered by this requirement, nothing new to build.

## Summary
Two rule-based product lists shown on the customer dashboard: **Featured** (admin-curated) and **Popular** (derived from real launch-count usage data across every customer). Together they are "Recommend products" — there is no third, separately-computed recommendation list.

## Actors
- Any customer (signed in or not — the underlying data is public, same as the product catalog)
- Platform administrator (`MANAGE_CATALOG`) — sets which products are featured, on the existing product edit form

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-001.1 | An admin can mark a product as featured (or clear it) from the existing product edit form. | Must |
| REQ-MKT-001.2 | `GET /products/recommendations` returns every ACTIVE featured product. | Must |
| REQ-MKT-001.3 | The same call returns up to 8 ACTIVE products, ranked by total launch count summed across every customer, most-launched first. | Must |
| REQ-MKT-001.4 | A product with zero launches, or that is not ACTIVE, never appears in the popular list. | Must |

## Out of scope
- Any AI/ML-based or collaborative-filtering recommendation model — the roadmap's own AI flag on this feature is informational only ([C12](../../../01-business/roadmap/open-decisions.md#c12)); a plain admin flag and existing usage data already satisfy all three functions.
- Personalized recommendations based on a specific customer's own purchase/browsing history — no such data model exists yet.
- Any UI ranking control beyond the fixed "most launches first" order.

## Dependencies
- Existing `Product` (new `featured` column, [V010](../../../../database/migrations/V010__recommendations_ticket_management.sql)), `ProductUsage` (Phase 16, unchanged).
- Existing `ProductRepository`, `ProductUsageRepository` (new aggregate query).
