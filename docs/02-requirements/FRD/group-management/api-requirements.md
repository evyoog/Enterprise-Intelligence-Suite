# API requirements — Group Management

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/organization/me/groups` | List the caller's organization's groups, each with its members | `MANAGE_USERS` | 200 | 403 |
| POST | `/organization/me/groups` | Create a group (body `{"name": "..."}`) | `MANAGE_USERS` | 200 (GroupDto) | 400, 403 |
| DELETE | `/organization/me/groups/{groupId}` | Delete a group | `MANAGE_USERS` | 204 | 403, 404 |
| POST | `/organization/me/groups/{groupId}/members/{memberId}` | Add a member | `MANAGE_USERS` | 200 (GroupDto) | 403, 404 |
| DELETE | `/organization/me/groups/{groupId}/members/{memberId}` | Remove a member | `MANAGE_USERS` | 200 (GroupDto) | 403, 404 |

## Request / response
```json
// POST /organization/me/groups
{ "name": "Engineering" }
```
```json
// 200
{
  "id": 3,
  "name": "Engineering",
  "members": [
    { "organizationMemberId": 42, "customerId": 9, "firstName": "Bob", "lastName": "Byte", "email": "bob@example.com", "orgRole": "MEMBER", "status": "ACTIVE", "lastReviewedAt": null }
  ]
}
```

OpenAPI contract: not maintained separately — `OrganizationController` (group endpoints) and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
