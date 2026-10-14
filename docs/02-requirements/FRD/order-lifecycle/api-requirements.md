# API requirements — Order Lifecycle

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/organization/me/orders` | Submit an order (BR-ORD-001, BR-ORD-007, BR-ORD-008): one product, or `items` and an optional `reason` | Authenticated, active org member | 200 (`OrderDto`) | 400, 404 (product/plan) |
| GET | `/organization/me/orders` | The caller's own orders (Track order) | Authenticated, active org member | 200 (`OrderDto[]`) | - |
| GET | `/organization/me/orders/pending` | Orders awaiting this caller's decision | `MANAGE_ORDERS` | 200 (`OrderDto[]`) | 403 |
| POST | `/organization/me/orders/{id}/approve` | Approve and provision (BR-ORD-002) | `MANAGE_ORDERS` | 200 (`OrderDto`) | 400, 403, 404 |
| POST | `/organization/me/orders/{id}/reject` | Reject (BR-ORD-003) | `MANAGE_ORDERS` | 200 (`OrderDto`) | 400, 403, 404 |
| POST | `/organization/me/orders/{id}/cancel` | Cancel the caller's own SUBMITTED order (BR-ORD-004) | Authenticated, the order's own requester | 200 (`OrderDto`) | 400, 404 |

`items` lists every product of the order (the fields above it are the first); `requestReason` is the requester's reason or `null`. Errors for the new fields: 400 "Choose at least one product.", "Each product can be in an order once.", "The reason can have up to 500 characters."; 404 for an unknown product or plan. A **direct purchase** by an organization administrator or a Manage billing holder is made through `POST /me/cart/checkout` ([cart-checkout API](../../../06-api/api-requirements/cart-checkout.md)); it creates an order that is already APPROVED and has no endpoint here. Approving an order (`POST …/{id}/approve`) provisions every product and creates one invoice.

Every method resolves "who is calling" from the JWT (`CurrentCustomerResolver`), never from the request body. Covered by the existing `/organization/me/**` authenticated() rule in `SecurityConfig`; the `MANAGE_ORDERS` check happens inside `OrderService`, same as every other organization self-service permission.

## Request / response
```json
// POST /organization/me/orders — one product (as before)
{ "productId": 3, "planId": 7 }
// or several products and a reason (C100 CA-1 B, CA-2)
{ "items": [ { "productId": 3, "planId": 7 }, { "productId": 4, "planId": null } ], "reason": "Needed for the Q4 launch" }
```
```json
// 200
{
  "id": 12,
  "productId": 3,
  "productName": "Valam.ai",
  "planId": 7,
  "planName": "Pro",
  "status": "SUBMITTED",
  "requestedByCustomerId": 41,
  "requestedByName": "Jane Member",
  "decidedByCustomerId": null,
  "decidedByName": null,
  "decidedAt": null,
  "decisionNote": null,
  "createdAt": "2027-01-05T09:00:00Z",
  "items": [ { "productId": 3, "productName": "Valam.ai", "planId": 7, "planName": "Pro" } ],
  "requestReason": null
}
```
```json
// POST /organization/me/orders/12/approve
{ "note": "Approved for Q1 rollout" }
```

OpenAPI contract: not maintained separately — `OrderController`/`OrderDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
