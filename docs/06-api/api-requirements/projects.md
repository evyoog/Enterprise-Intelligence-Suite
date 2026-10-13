# API: Projects (REQ-TEN-009)

Base path `/organization/me/projects`. Permissions as in BR-PRJ-007.

| Method | Path | Purpose | Permission |
|---|---|---|---|
| GET | `/organization/me/projects` | List projects with members and products | `MANAGE_USERS` or `MANAGE_PRODUCT_ACCESS` |
| POST | `/organization/me/projects` | `{name, description}` create | `MANAGE_USERS` |
| PUT | `/organization/me/projects/{id}` | `{name, description}` rename or describe | `MANAGE_USERS` |
| DELETE | `/organization/me/projects/{id}` | Delete; takes back granted access | `MANAGE_USERS` |
| POST | `/organization/me/projects/{id}/members/{memberId}` | Add a member | `MANAGE_USERS` |
| DELETE | `/organization/me/projects/{id}/members/{memberId}` | Remove a member | `MANAGE_USERS` |
| PUT | `/organization/me/projects/{id}/products` | `{productId, productRole}` add or change role | `MANAGE_PRODUCT_ACCESS` |
| DELETE | `/organization/me/projects/{id}/products/{productId}` | Remove a product | `MANAGE_PRODUCT_ACCESS` |

Project: `{id, name, description, createdAt, members:[{memberId, name, email, orgRole}], products:[{productId, productName, productRole}]}`. Errors: 400 invalid input or a product that is not available or not subscribed, 403 not allowed, 404 not found or another organization, 409 duplicate name.
Direct access (`PUT`/`DELETE /organization/me/members/{id}/products...`) is unchanged, except that revoking access that comes from a project answers 409 (BR-PRJ-006).
