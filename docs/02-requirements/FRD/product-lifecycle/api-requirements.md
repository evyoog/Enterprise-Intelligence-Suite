# API requirements — Product Lifecycle & Structure

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/products/{id}/publish` | Publish (set ACTIVE) | `MANAGE_CATALOG` | 200 | 404 |
| POST | `/products/{id}/retire` | Retire (set RETIRED) | `MANAGE_CATALOG` | 200 | 404 |
| PUT | `/products/{id}` | Now also accepts `parentProductId`, `variantLabel`, `dependsOnProductIds` | `MANAGE_CATALOG` | 200 | 400 (self-dependency), 404 |
| POST | `/products` | Now also accepts `parentProductId`, `variantLabel`, `dependsOnProductIds` | `MANAGE_CATALOG` | 201 | 400 |
| DELETE | `/products/{id}` | Unchanged path, now also refused if this product is a parent or a dependency target | `MANAGE_CATALOG` | 204 | 409 |

## Request / response
```json
// PUT /products/42
{
  "name": "PMS Enterprise",
  "price": 199.00,
  "parentProductId": 7,
  "variantLabel": "Enterprise",
  "dependsOnProductIds": [3, 9]
}
```
```json
// 200 — ProductDto (existing shape, plus:)
{
  "id": 42,
  "version": 4,
  "parentProductId": 7,
  "variantLabel": "Enterprise",
  "dependsOnProductIds": [3, 9],
  "status": "ACTIVE"
}
```

OpenAPI contract: not maintained separately for this feature — the implemented `ProductController` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
