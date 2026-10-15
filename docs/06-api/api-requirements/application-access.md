# API requirements — Application access requests

Requirement: [REQ-TEN-010](../../02-requirements/FRD/application-access/requirement.md). Rules: [business-rules.md](../../02-requirements/FRD/application-access/business-rules.md). Controller: `AccessRequestController` (`backend/src/main/java/com/vyoog/eisplatform/modules/registration/controller/AccessRequestController.java`); service: `AccessRequestService`.

**Who may call:** `/organization/me/**` is authenticated-only — any ACTIVE member of their own organization. The two admin-side endpoints (`GET .../access-requests/all`, `approve`, `reject`) additionally require `MANAGE_PRODUCT_ACCESS` (held by every `ORG_ADMIN`, or delegated — [REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md)), checked inside the service; a caller without it, or acting on another organization's request, gets 403. A nonexistent request id gets 404.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| POST | `/organization/me/access-requests` | Create an access request (`CreateAccessRequest(productId, message)`) | Any ACTIVE member | 200 (`AccessRequestDto`, PENDING) | 400 (no active subscription, already has access, already a pending request), 401 |
| GET | `/organization/me/access-requests` | The caller's own requests ("My requests"), newest first | Any ACTIVE member | 200 (`AccessRequestDto[]`) | 401 |
| POST | `/organization/me/access-requests/{id}/cancel` | Cancel the caller's own request while PENDING | The request's own requester | 200 (`AccessRequestDto`, CANCELLED) | 400 (not PENDING), 404 (not found / not the caller's) |
| DELETE | `/organization/me/products/{productId}/access` | Remove the caller's own ACTIVE access to a product, immediately | Any ACTIVE member | 200 (no body) | 400 (no active access to remove), 401 |
| GET | `/organization/me/access-requests/all` | Every access request for the caller's organization, newest first | `MANAGE_PRODUCT_ACCESS` | 200 (`AccessRequestDto[]`) | 403 |
| POST | `/organization/me/access-requests/{id}/approve` | Approve a PENDING request — grants access (same seat check as a direct grant) | `MANAGE_PRODUCT_ACCESS` | 200 (`AccessRequestDto`, APPROVED) | 400 (not PENDING, no free seat), 403 (not the request's own organization, or caller lacks the permission), 404 |
| POST | `/organization/me/access-requests/{id}/reject` | Reject a PENDING request (`DecideAccessRequest(note)`, required) | `MANAGE_PRODUCT_ACCESS` | 200 (`AccessRequestDto`, REJECTED) | 400 (not PENDING, blank note), 403, 404 |

## Request / response
```json
// POST /organization/me/access-requests
{ "productId": 7, "message": "Need it for my work" }

// 200
{
  "id": 42, "productId": 7, "productName": "Valam.ai",
  "memberId": 15, "memberName": "Asha Rao",
  "status": "PENDING", "message": "Need it for my work",
  "createdAt": "2026-10-15T10:00:00Z",
  "decidedByName": null, "decidedAt": null, "decisionNote": null
}
```

```json
// POST /organization/me/access-requests/42/reject
{ "note": "Not needed for your role" }
```

Reused, unchanged by this feature: `POST /organization/me/orders` and the order approve/reject/cancel endpoints ([order-lifecycle API](../../02-requirements/FRD/order-lifecycle/api-requirements.md)); the 3-day pending-order reminder (`OrderReminderJob`) is an internal scheduled job with no new endpoint — it only sends a notification and sets `orders.reminded_at`.

OpenAPI contract: not yet written as a separate file; see `docs/06-api/openapi/README.md` for the convention when one is added.
