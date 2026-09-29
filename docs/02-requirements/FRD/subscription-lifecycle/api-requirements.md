# API requirements — Subscription Lifecycle

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/me/subscriptions/{id}/suspend` | Suspend the caller's own subscription | Authenticated (any customer) | 200 (`SubscriptionDto`) | 400 (BR-SUB-001), 404 (BR-SUB-004) |
| POST | `/me/subscriptions/{id}/reactivate` | Reactivate the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-002), 404 |
| POST | `/me/subscriptions/{id}/cancel` | Cancel the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-003), 404 |
| POST | `/me/subscriptions/{id}/renew` | Renew the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-005), 404 |
| PATCH | `/me/subscriptions/{id}/plan` | Change (or clear) the plan on the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-007), 404 (subscription, or unknown plan) |

Every method above resolves "who is calling" from the JWT (`CurrentCustomerResolver`), never from anything the client sends — see `SubscriptionController`.

## Request / response
```json
// PATCH /me/subscriptions/42/plan
{ "planId": 7 }
```
```json
// 200
{
  "id": 42,
  "productId": 3,
  "productName": "Valam.ai",
  "status": "ACTIVE",
  "startedAt": "2026-10-01T00:00:00Z",
  "expiresAt": null,
  "planId": 7,
  "planName": "Pro"
}
```
```json
// PATCH /me/subscriptions/42/plan — clear the plan
{ "planId": null }
```
```json
// 400 — cancelled subscription
{ "message": "A cancelled subscription's plan cannot be changed." }
```

OpenAPI contract: not maintained separately — `SubscriptionController`/`SubscriptionDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
