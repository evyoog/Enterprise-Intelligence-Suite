# API requirements — Member Lifecycle & Access Review

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| PATCH | `/organization/me/members/{memberId}/status` | Suspend/reactivate/remove (body `{"action": "SUSPEND"\|"REACTIVATE"\|"REMOVE"}`) | `MANAGE_USERS`, same organization | 200 (OrgMemberDto) | 400 (last admin, or reactivating a removed member), 403, 404, 409 (seat limit) |
| POST | `/organization/me/members/{memberId}/access-review` | Stamp a review | `MANAGE_USERS`, same organization | 200 (OrgMemberDto) | 403, 404 |

## Request / response
```json
// PATCH /organization/me/members/42/status
{ "action": "SUSPEND" }
```
```json
// 200
{
  "organizationMemberId": 42,
  "customerId": 9,
  "firstName": "Bob",
  "lastName": "Byte",
  "email": "bob@example.com",
  "orgRole": "MEMBER",
  "status": "SUSPENDED",
  "lastReviewedAt": null
}
```

OpenAPI contract: not maintained separately — `OrganizationController`/`OrgMemberDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
