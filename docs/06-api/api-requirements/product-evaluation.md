# API requirements — Product evaluation

Requirement: [REQ-MKT-005](../../02-requirements/FRD/product-evaluation/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/product-evaluation/business-rules.md). Decision: [C102](../../01-business/roadmap/open-decisions.md#c102) (TR-1 note, TR-2 A). Paths are relative to `/api`. Error bodies are `{"timestamp", "status", "error", "message"}` from `GlobalExceptionHandler` (a 409 also carries `"code": "INVALID_STATE"`). Data model: [trials.md](../../07-database/data-model/trials.md).

**Permissions** (`SecurityConfig`). `GET /products/**` is public, so **the compare page needs no sign-in**. `POST /me/**` and `POST /organization/me/**` need a signed-in customer (401 without a token). The organization trial then needs an **organization administrator or Manage billing** (`MANAGE_ORGANIZATION_BILLING`), checked in `TrialService` (403).

## Endpoints
| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| POST | `/me/trials` | An individual starts a free trial of a product | Signed-in customer | 200 `SubscriptionDto` (`trial: true`) | 400, 401, 404, 409 |
| POST | `/organization/me/trials` | The caller's organization starts a free trial | Organization administrator, or `MANAGE_ORGANIZATION_BILLING` | 200 `SubscriptionDto` (`trial: true`) | 400, 401, 403, 404, 409 |
| POST | `/me/demo-requests` | Ask for a demo: creates a support ticket in the category "Demo request" | Signed-in customer | 200 `SupportTicketDto` | 400, 401, 404 |
| GET | `/products/compare?items=…&seats=…` | Compare 2 to 4 products or plans with a cost estimate | Public (a token adds the tax estimate) | 200 `Comparison` | 400, 404 |
| POST, PUT | `/products`, `/products/{id}` | Existing product create and update; now carry `trialDays` | `MANAGE_CATALOG` | 200 `ProductDto` | 400, 401, 403 |

### Start a trial
```json
// POST /me/trials   (the organization trial is the same body at /organization/me/trials)
{ "productId": 7 }

// 200
{ "id": 91, "productId": 7, "productName": "Valam.ai", "status": "ACTIVE",
  "startedAt": "2027-03-01T10:00:00Z", "expiresAt": "2027-03-08T10:00:00Z",
  "planId": 21, "planName": "Enterprise", "quantity": 1, "autoRenew": false,
  "pendingPlanId": null, "pendingPlanName": null, "pendingQuantity": null, "graceEndsAt": null, "trial": true }
```
`planId` is the product's highest-priced plan (null for a product without plans). `quantity` is 1 for an individual and the organization's seats for an organization. `expiresAt` is the end of the trial.

| Case | Status and message |
|---|---|
| Product not published or not found | 404 "Product not found" |
| Product has no trial days | 400 "{product} does not offer a free trial." |
| Audience does not fit | 409 "{product} is available to organizations only." / "… individuals only." |
| Owner already has or had a subscription (any status) | 400 "You already have, or had, a subscription to {product}, so a free trial is not available." / "Your organization already has, or had, …" |
| Not a member of an organization (organization call) | 404 "You are not a member of an organization" |
| Organization not in good standing | 403 "Your organization's account is not currently active" |
| Member who is not an administrator and has no Manage billing | 403 "You do not have permission to do this" |

### Request a demo
```json
// POST /me/demo-requests   (preferredTime and message are optional, up to 1000 characters each)
{ "productId": 7, "preferredTime": "Next Tuesday afternoon", "message": "We are 40 people." }

// 200 (the ticket, as in ticket-management)
{ "id": 214, "requestedByCustomerId": 5, "subject": "Demo request: Valam.ai", "category": "Demo request",
  "description": "I would like a demo of Valam.ai (product 7).\n\nPreferred time: Next Tuesday afternoon\n\nMessage: We are 40 people.",
  "priority": "MEDIUM", "status": "OPEN", "createdAt": "2027-03-01T10:00:00Z",
  "firstRespondedAt": null, "firstResponseDueAt": "2027-03-01T18:00:00Z", "resolutionDueAt": "2027-03-04T13:30:00Z",
  "firstResponseState": null, "resolutionState": null }
```
No product id = 400 "Choose a product."; a product that does not exist or is not published = 404 "Product not found"; a field over 1000 characters = 400 "Keep each field under 1000 characters." The ticket is then read and worked through the [ticket-management](../../02-requirements/FRD/ticket-management/api-requirements.md) endpoints (`/me/tickets`, `/admin/support/tickets`); the SLA states are returned to the support team only ([sla-management](sla-management.md)).

### Compare
`items` is a comma-separated list of `productId` or `productId:planId`; `seats` is optional (default 1).

```json
// GET /products/compare?items=7:21,9:30&seats=3
{ "seats": 3,
  "items": [
    { "product": { "id": 7, "name": "Valam.ai", "status": "ACTIVE", "trialDays": 14, "plans": [ … ], … },
      "plan": { "id": 21, "name": "Pro", "price": 100.00, "billingPeriod": "MONTHLY", "currency": "INR", "includedFeatures": "SSO, Audit log", … },
      "currency": "INR", "unitPrice": 100.00, "billingPeriod": "MONTHLY", "seats": 3, "subtotal": 300.00,
      "tax": { "status": "NOT_SIGNED_IN", "amount": null, "total": null, "message": "Sign in to see the tax estimate." },
      "includedFeatures": ["SSO", "Audit log"], "trialDays": 14 },
    { "product": { "id": 9, … }, "plan": { "id": 30, "name": "Team", "price": 250.50, … },
      "currency": "INR", "unitPrice": 250.50, "billingPeriod": "MONTHLY", "seats": 3, "subtotal": 751.50,
      "tax": { "status": "NOT_SIGNED_IN", … }, "includedFeatures": [ … ], "trialDays": null } ] }
```
| Field | Meaning |
|---|---|
| `seats` | The seats used for every item (1 to 100 000, default 1) |
| `items[].product` | The full `ProductDto` (with its plans and `trialDays`) |
| `items[].plan` | The chosen plan, else the product's **lowest-priced plan**; **null** for a product without plans |
| `items[].currency` | The plan's currency; `USD` for a product without a plan. **Never converted** |
| `items[].unitPrice`, `billingPeriod` | The plan's price and period (`MONTHLY`, `YEARLY`, `ONE_TIME`); the product's flat price and null without a plan |
| `items[].subtotal` | `unitPrice` x `seats`, 2 decimals, half up |
| `items[].tax.status` | `ESTIMATED` (`amount` and `total` set), `NOT_SIGNED_IN`, `NEEDS_BILLING_DETAILS` or `AT_CHECKOUT` ("Tax is worked out at checkout"); `message` explains the last three |
| `items[].includedFeatures` | The plan's included features, else the product's feature tags |
| `items[].trialDays` | The product's trial days, or null |

| Case | Status and message |
|---|---|
| `items` missing or empty, fewer than 2 or more than 4 | 400 "Choose 2 to 4 products or plans to compare." |
| A part that is not a number or a `product:plan` pair | 400 "\"x\" is not a product or a product:plan pair." |
| The same item twice | 400 "The same item is chosen twice." |
| `seats` below 1 or above 100 000 | 400 "Seats must be between 1 and 100000." |
| A plan that does not belong to its product | 400 "Plan {id} does not belong to {product}." |
| A product that does not exist or is not published | 404 "Product not found: {id}" |

### Trial days on the product
`ProductCreateRequest` (create and update) and `ProductDto` carry `trialDays`: an integer from 1 to 90, or null for no trial. A value outside the range is 400 ("trialDays: …"). An update replaces every field, so an update that omits `trialDays` clears the trial.

## Fields and events that changed
| Where | Change |
|---|---|
| `SubscriptionDto` | New `trial` (boolean) on every subscription endpoint, individual and organization (`GET /me/subscriptions`, `GET /organization/me/subscriptions`, the action endpoints). `expiresAt` is the end of the trial; `autoRenew` is false |
| `SubscriptionCreated`, `SubscriptionChanged` and the other subscription events | The payload now has `trial` (boolean) next to `subscriptionId`, `productId`, `planId`, `ownerType`, `ownerCustomerId`, `ownerOrganizationId`, `status`, `expiresAt`. A trial publishes `SubscriptionCreated` with `trial: true` when it starts and `SubscriptionChanged` with `status: "EXPIRED"` when it ends ([REQ-ORD-002](../../02-requirements/FRD/provisioning-contract/requirement.md)) |
| `POST /me/subscriptions/{id}/renew` | 400 for a trial: "A free trial cannot be renewed. Subscribe to keep the product after the trial." |
| `PATCH /me/subscriptions/{id}/auto-renew`, `PATCH /organization/me/subscriptions/{id}/auto-renew` | 400 for a trial: "A free trial does not renew. Subscribe to keep the product after the trial." |
| `PATCH /me/subscriptions/{id}/plan`, `PATCH /organization/me/subscriptions/{id}/plan` | 400 for a trial: "A free trial has all features on the highest plan. Subscribe to choose a plan." |
| `PATCH /organization/me/subscriptions/{id}/seats` | 400 for a trial: "A free trial has the organization's seats. Subscribe to change them." |
| `POST /me/tickets` (existing) | Unchanged; a demo request uses the same service with the category set |
| Cart checkout | Unchanged; a running trial is reported as `ALREADY_SUBSCRIBED` ([BR-EVL-016](../../02-requirements/FRD/product-evaluation/business-rules.md)) |

## Jobs, notices and audit
| Item | Detail |
|---|---|
| `SubscriptionExpiryJob` | Hourly. Expires overdue subscriptions (a trial at once, with no grace), then `remindEndingTrials` |
| Notices (individual owner) | "Free trial started: {product}", "Your free trial of {product} ends soon" (once, 3 days before), "Free trial ended: {product}". Not sent for an organization-owned trial |
| Audit | `TRIAL_STARTED`, `TRIAL_EXPIRED`, plus `TICKET_CREATED` for a demo request |

Frontend clients: `frontend/src/api/evaluationApi.ts` (`startTrial`, `startOrganizationTrial`, `requestDemo`), `frontend/src/api/compareApi.ts` (`compare`). OpenAPI contract: not maintained separately; `EvaluationController`, `EvaluationDtos`, `SubscriptionDto` and this file are the source of truth ([C14](../../01-business/roadmap/open-decisions.md#c14)).
