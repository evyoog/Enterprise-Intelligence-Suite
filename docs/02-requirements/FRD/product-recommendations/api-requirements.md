# API requirements — Product Recommendations

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/products/recommendations` | Featured + popular products | Public | 200 (`RecommendationsDto`) | - |
| PUT | `/products/{id}` | Now also accepts `featured` (nullable, defaults false) | `MANAGE_CATALOG` | 200 (`ProductDto`, now with `featured`) | 400, 403, 404 |
| POST | `/products` | Now also accepts `featured` on creation | `MANAGE_CATALOG` | 201 (`ProductDto`, now with `featured`) | 400, 403 |

## Request / response
```json
// GET /products/recommendations
{
  "featured": [
    { "id": 3, "name": "Valam.ai", "category": "Analytics", "imageUrl": null, "launchUrl": "https://valam.example" }
  ],
  "popular": [
    { "id": 7, "name": "Varthan.ai", "category": "Sales", "imageUrl": null, "launchUrl": "https://varthan.example" }
  ]
}
```

OpenAPI contract: not maintained separately — `RecommendationController`/`RecommendationsDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
