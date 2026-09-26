# API requirements — Plan Management

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/products` | `plans[]` now also accepts `currency`, `usageLimit`, `includedFeatures`, `usagePrice`, `tierPricing`, `overageCharge` | `MANAGE_CATALOG` | 201 | 400 |
| PUT | `/products/{id}` | Same, on the replaced `plans[]` | `MANAGE_CATALOG` | 200 | 400 |

## Request / response
```json
// POST /products, one plan
{
  "name": "Pro",
  "price": 49.00,
  "billingPeriod": "MONTHLY",
  "currency": "EUR",
  "usageLimit": 1000,
  "includedFeatures": "SSO, 5 seats, priority support",
  "usagePrice": 0.02,
  "overageCharge": 0.05,
  "tierPricing": "1-1000 calls included, $0.02/call after"
}
```

OpenAPI contract: not maintained separately — `ProductController`/`ProductPlanDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
