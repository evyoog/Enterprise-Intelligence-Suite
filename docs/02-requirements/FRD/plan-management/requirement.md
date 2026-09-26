# REQ-CAT-002 — Plan Management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-26 ([C33](../../../01-business/roadmap/open-decisions.md#c33))

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-CAT-002 |
| Application | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md) |
| Application code | `APP-CAT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 02.03.01.03 | Define usage limits | [02 Product & Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md#feature-020301-plan-definition) |
| 02.03.01.04 | Define included features | same |
| 02.03.02.02 | Define usage price | [same page](../../../01-business/roadmap/applications/02-product-catalog-management.md#feature-020302-pricing-models) |
| 02.03.02.03 | Define tier price | same |
| 02.03.02.04 | Define overage charge | same |

02.03.01.01 Create plan, 02.03.01.02 Define billing frequency and 02.03.02.01 Define subscription price are **not** in this FRD — already built (`ProductPlan.price`/`billingPeriod`).

## Summary
Each subscription plan gains a currency, a usage limit with a free-text included-features list, a per-unit usage price, an overage charge, and a free-text tier-pricing description — shown on the admin plan editor and the plan card. These are data fields the admin fills in; no metering, tiered-billing or currency-conversion engine is built by this feature (that is 08 Billing & Payments, a later sprint).

## Actors
- Platform administrator (holding `MANAGE_CATALOG`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-CAT-002.1 | Each plan has a currency, one of USD, EUR, GBP or INR (default USD). | P0 |
| REQ-CAT-002.2 | Each plan may have a usage limit (a number) and an included-features free-text list. | P0 |
| REQ-CAT-002.3 | Each plan may have a usage price (per unit) and an overage charge (per unit beyond the usage limit). | P0 |
| REQ-CAT-002.4 | Each plan may have a free-text tier-pricing description. | P0 |
| REQ-CAT-002.5 | All five fields are optional; a plan with none of them behaves exactly as before this feature. | P0 |

## Out of scope
- Currency conversion, multi-currency checkout, or any other currency actually affecting a charge (C33)
- Enforcing/metering the usage limit or overage charge against real usage (needs 08 Billing)
- A structured tiered-pricing engine (bands, thresholds) — `tierPricing` is display text only
- A structured feature-flag model for `includedFeatures` — free text only

## Dependencies
- Existing `ProductPlan`, `ProductPlanCreateRequest`, `ProductPlanDto`.
- New columns: `product_plans.currency`, `.usage_limit`, `.included_features`, `.usage_price`, `.tier_pricing`, `.overage_charge` ([V006](../../../../database/migrations/V006__product_structure_plan_pricing_member_lifecycle_groups.sql)).
