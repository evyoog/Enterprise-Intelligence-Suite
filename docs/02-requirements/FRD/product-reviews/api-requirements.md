# API requirements — Product Reviews & Ratings

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/products/{productId}/reviews` | View ratings (BR-MKT-009) | Public | 200 (`ProductRatingSummaryDto`) | - |
| PUT | `/me/products/{productId}/review` | Submit review / Rate product (BR-MKT-005–.007) | Authenticated | 200 (`ProductReviewDto`) | 400, 404 |
| GET | `/me/products/{productId}/review` | The caller's own review for this product | Authenticated | 200 (`ProductReviewDto`) | 404 (none submitted yet) |
| GET | `/admin/reviews` | Every review, any status | `MANAGE_REVIEWS` | 200 (`ProductReviewDto[]`) | 403 |
| POST | `/admin/reviews/{id}/approve` | Moderate — approve (BR-MKT-008) | `MANAGE_REVIEWS` | 200 (`ProductReviewDto`) | 400, 403, 404 |
| POST | `/admin/reviews/{id}/reject` | Moderate — reject (BR-MKT-008) | `MANAGE_REVIEWS` | 200 (`ProductReviewDto`) | 400, 403, 404 |

## Request / response
```json
// PUT /me/products/3/review
{ "rating": 4, "comment": "Solid analytics, a bit slow to load." }
```
```json
// GET /products/3/reviews
{
  "averageRating": 4.2,
  "reviewCount": 5,
  "reviews": [
    { "id": 12, "productId": 3, "productName": "Valam.ai", "customerId": 41, "customerName": "Jane Customer",
      "rating": 4, "comment": "Solid analytics, a bit slow to load.", "status": "APPROVED", "createdAt": "2027-03-02T10:00:00Z" }
  ]
}
```

OpenAPI contract: not maintained separately — `ProductReviewController`/`MyProductReviewController`/`AdminProductReviewController`/`ProductReviewDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
