# API requirements — Seats and quantity (organization scope)

Requirement: [REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/subscription-seats/business-rules.md). Subscriptions of another organization: 404.

**Permissions** ([C100](../../01-business/roadmap/open-decisions.md#c100) SE-2 A): **changing** seats needs the organization permission `MANAGE_ORGANIZATION_BILLING` ("Manage billing"; organization administrators have it). **Reading** the organization's subscriptions and a seat summary needs `MANAGE_ORGANIZATION_BILLING` or `MANAGE_SUBSCRIPTIONS`. Any other member: 403.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/subscriptions` | The organization's subscriptions, each with `quantity`, `pendingQuantity`, `autoRenew` and `graceEndsAt` | 200 | 403 |
| GET | `/organization/me/subscriptions/{id}/seats` | Seat summary | 200 | 403, 404 |
| PATCH | `/organization/me/subscriptions/{id}/seats` | Body `{ "quantity": 20 }` (1–100 000). **An increase takes effect now and is charged now** on its own invoice; **a decrease is stored and takes effect at the next renewal, with no credit**; the current quantity withdraws a pending decrease. Returns the summary with `effect` | 200 | 400 (quantity outside 1–100 000), 403, 404, 409 (`INVALID_STATE`: below the seats in use, not ACTIVE or SUSPENDED, or the term has ended) |

`effect` and `invoiceId` are filled only in the answer to a change:

| `effect` | Meaning |
|---|---|
| `NOW` | The quantity changed at once. An increase carries `invoiceId` (the invoice for the added seats; null when nothing was due: a free plan or no days left). A decrease on a subscription with no renewal date also applies now |
| `AT_RENEWAL` | A decrease was scheduled: `pendingQuantity` holds it, `quantity` is unchanged, no invoice |
| `WITHDRAWN` | The quantity asked for equals the current one and a pending decrease was cleared |
| `UNCHANGED` | The quantity asked for equals the current one and nothing was pending |

```json
// GET /organization/me/subscriptions/{id}/seats
{ "subscriptionId": 42, "productName": "Valam.ai", "quantity": 15, "inUse": 12, "minimum": 12,
  "organizationSeatLimit": 15, "maximum": 100000,
  "pendingQuantity": null, "unitAmount": 100000, "currency": "INR",
  "remainingDays": 19, "termDays": 30, "renewsAt": "2026-11-02T10:00:00Z",
  "effect": null, "invoiceId": null }

// PATCH … { "quantity": 18 }  (an increase)
{ "subscriptionId": 42, "productName": "Valam.ai", "quantity": 18, "inUse": 12, "minimum": 12,
  "organizationSeatLimit": 18, "maximum": 100000, "pendingQuantity": null,
  "unitAmount": 100000, "currency": "INR", "remainingDays": 19, "termDays": 30,
  "renewsAt": "2026-11-02T10:00:00Z", "effect": "NOW", "invoiceId": 311 }

// PATCH … { "quantity": 10 }  (not below 12 in use: 409; with 5 seats in use, a decrease)
{ …, "quantity": 15, "pendingQuantity": 10, "effect": "AT_RENEWAL", "invoiceId": null }
```
| Field | Notes |
|---|---|
| `inUse` | Named seats per product ([C90](../../01-business/roadmap/open-decisions.md#c90)): members who have this product ON |
| `minimum` | The lowest quantity allowed now: `max(1, inUse)` |
| `unitAmount`, `currency` | The price of **one seat for one term**, in the currency's smallest unit; 0 and `null` for a free plan (per-seat pricing is an engineering default to confirm, [REQ-SUB-003.7](../../02-requirements/FRD/subscription-seats/requirement.md)) |
| `remainingDays`, `termDays` | What an increase now is charged for: added seats × `unitAmount` × `remainingDays` ÷ `termDays` (30 Monthly, 365 Yearly); `remainingDays` is rounded up |
| `renewsAt` | The subscription's term end: when a pending decrease takes effect |
| `pendingQuantity` | A decrease that takes effect at the next renewal; `null` when none |

The seat-increase invoice's line reads "Added seats: {product} — {plan}", quantity = the added seats; it is taxed like any invoice and takes no coupon ([billing-payments.md](billing-payments.md#invoices-with-a-coupon-and-per-seat-lines)).

## Related subscription endpoints (same organization scope)
| Method | Path | Permission |
|---|---|---|
| PATCH | `/organization/me/subscriptions/{id}/auto-renew` | Body `{ "autoRenew": false }` — Manage billing or Manage subscriptions; see [renewal-reminders.md](renewal-reminders.md#auto-renew-and-the-grace-period) |
| POST | `/organization/me/subscriptions/{id}/suspend`, `/reactivate`, `/cancel`; PATCH `…/plan`; DELETE `…/plan/scheduled` | Manage subscriptions ([REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md) rules; unchanged) |
