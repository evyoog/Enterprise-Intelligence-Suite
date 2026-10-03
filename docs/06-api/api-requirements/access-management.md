# API requirements — Access management (organization scope)

Requirement: [REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md). Rules: [business-rules.md](../../02-requirements/FRD/access-management/business-rules.md), [BR-ACC-001](../../03-business-rules/BR-ACC-001-effective-access.md).

**Who may call:** organization admins and delegated administrators (*Manage access*); others get 403. A member of another organization is 404. Guard-rail refusals are 403 with a message naming the rule; a seat refusal is 409 `NO_FREE_SEAT`. Feature permissions are identified by their code; products by their product ID.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/access/permissions` | The feature permission catalogue: code, name, description, area, and whether the caller may grant it | 200 | 403 |
| GET | `/organization/me/access/members?page=&size=&q=&role=&productId=&custom=&reviewedBefore=` | Members with role, status, product count, custom-access count, last reviewed. Paged (default 20) | 200 | 400, 403 |
| GET | `/organization/me/access/members/{memberId}` | One member's effective access, each item with its source (`ROLE` or `OVERRIDE`) and, when the caller cannot change it, `lockedReason` | 200 | 403, 404 |
| PUT | `/organization/me/access/members/{memberId}/overrides` | Set overrides. Body: `{ "permissions": { "MANAGE_SUBSCRIPTIONS": true }, "products": { "7": false }, "reset": ["permission:MANAGE_ORDERS"] }` — `true`/`false` stores an override, `reset` removes one | 200 (new effective access) | 400, 403, 404, 409 |
| DELETE | `/organization/me/access/members/{memberId}/overrides` | Reset all the member's overrides to role defaults | 200 | 403, 404 |
| GET | `/organization/me/access/roles/{role}/defaults` | Role defaults of `ORG_ADMIN` or `MEMBER` (admin: all permissions locked) | 200 | 400, 403 |
| PUT | `/organization/me/access/roles/{role}/defaults` | Save role defaults. Body like the overrides body without `reset`. Query `preview=true` returns the impact without saving | 200 (`affectedMembers`) | 400, 403 |
| POST | `/organization/me/access/bulk` | Grant or remove one item for several members. Body `{ "action": "GRANT", "item": "product:7", "memberIds": [..], "preview": true }` | 200 (`applied`, `skipped[{memberId, reason}]`) | 400, 403 |

Reused, unchanged: `PATCH /organization/me/members/{id}/role` (role, last-admin guard; now also BR-3/BR-4), `POST /organization/me/members/{id}/access-review` (mark reviewed), `GET /organization/me/audit-logs` (Activity tab, filtered to access actions).

Organization subscription actions (D16, BR-11), new, requiring *Manage subscriptions*: `POST /organization/me/subscriptions/{id}/suspend`, `/reactivate`, `/cancel`, `PATCH /organization/me/subscriptions/{id}/plan`; the existing `PATCH /organization/me/subscriptions/{id}/seats` moves from `MANAGE_ORGANIZATION` to *Manage subscriptions*.

```json
// GET /organization/me/access/members/42
{
  "memberId": 42, "name": "Asha Rao", "email": "asha@example.com", "role": "MEMBER", "status": "ACTIVE",
  "lastReviewedAt": "2026-09-30T10:00:00Z", "lastReviewedBy": "Ravi Kumar",
  "products": [ { "productId": 7, "name": "Valam.ai", "plan": "Standard", "enabled": true, "source": "ROLE",
                  "seats": { "inUse": 12, "quantity": 15 }, "lockedReason": null } ],
  "permissions": [ { "code": "MANAGE_SUBSCRIPTIONS", "name": "Manage subscriptions", "area": "SUBSCRIPTIONS",
                     "enabled": true, "source": "OVERRIDE", "lockedReason": null } ]
}
```
