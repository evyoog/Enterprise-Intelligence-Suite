# API requirements — Subscription Lifecycle

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/me/subscriptions/{id}/suspend` | Suspend the caller's own subscription | Authenticated (any customer) | 200 (`SubscriptionDto`) | 400 (BR-SUB-001), 404 (BR-SUB-004) |
| POST | `/me/subscriptions/{id}/reactivate` | Reactivate the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-002), 404 |
| POST | `/me/subscriptions/{id}/cancel` | Cancel the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-003), 404 |
| POST | `/me/subscriptions/{id}/renew` | Renew the caller's own subscription | Authenticated | 200 (`SubscriptionDto`) | 400 (BR-SUB-005), 404 |
| PATCH | `/me/subscriptions/{id}/plan` | Change (or clear) the plan on the caller's own subscription. An upgrade is applied now; a downgrade is **scheduled** for the next renewal (BR-SUB-011) | Authenticated | 200 (`SubscriptionDto`; a scheduled downgrade shows `pendingPlanId`/`pendingPlanName` and an unchanged `planId`) | 400 (BR-SUB-007), 404 (subscription, or unknown plan) |
| DELETE | `/me/subscriptions/{id}/plan/scheduled` | Withdraw a scheduled downgrade ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1). With nothing scheduled it changes nothing | Authenticated | 200 (`SubscriptionDto`) | 404 (BR-SUB-004) |
| PATCH | `/organization/me/subscriptions/{id}/plan` | The same plan change for an **organization** subscription (REQ-SUB-003, C65) | Organization permission `MANAGE_SUBSCRIPTIONS` | 200 (`SubscriptionDto`) | 400, 403, 404 |
| DELETE | `/organization/me/subscriptions/{id}/plan/scheduled` | Withdraw a scheduled downgrade on an organization subscription | `MANAGE_SUBSCRIPTIONS` | 200 (`SubscriptionDto`) | 403, 404 |

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
  "planName": "Pro",
  "quantity": 1,
  "autoRenew": true,
  "pendingPlanId": null,
  "pendingPlanName": null
}
```
`pendingPlanId` and `pendingPlanName` are set only while a downgrade is scheduled ([C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1); the plan in `planId` is unchanged until the subscription renews.
```json
// PATCH /me/subscriptions/42/plan { "planId": 8 }  — Basic is cheaper per month than Pro: scheduled, not applied
{ "id": 42, "planId": 7, "planName": "Pro", "expiresAt": "2026-11-02T18:29:00Z", "pendingPlanId": 8, "pendingPlanName": "Basic" }
```
```json
// DELETE /me/subscriptions/42/plan/scheduled  — withdrawn
{ "id": 42, "planId": 7, "planName": "Pro", "pendingPlanId": null, "pendingPlanName": null }
```
```json
// PATCH /me/subscriptions/42/plan — clear the plan
{ "planId": null }
```
```json
// 400 — cancelled subscription
{ "message": "A cancelled subscription's plan cannot be changed." }
```

Audit actions: `SUBSCRIPTION_PLAN_CHANGED` (immediate change, and the scheduled downgrade being applied at renewal), `SUBSCRIPTION_PLAN_CHANGE_SCHEDULED`, `SUBSCRIPTION_PLAN_CHANGE_WITHDRAWN`. A `SubscriptionChanged` platform event is published when a plan change is requested, whether applied or scheduled.

OpenAPI contract: not maintained separately — `SubscriptionController`/`SubscriptionDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
