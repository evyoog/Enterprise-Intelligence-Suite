# API requirements — Organization hierarchy

Base path `/organization/me/org-hierarchy`. All calls need an authenticated customer with `MANAGE_ORGANIZATION` (checked in the service); any other organization's ids answer 404.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/` | Whole tree as a flat list (creates root and default levels on first call) with levels | 200 `{levels, nodes}` | 403 |
| GET | `/nodes/{id}` | Node detail: path, members | 200 | 403, 404 |
| GET | `/nodes/{id}/history` | Move history, newest first | 200 | 403, 404 |
| POST | `/nodes` | Create `{parentId, name, type, code?, description?, sortOrder?}` | 201 | 400, 403, 404, 409 |
| PUT | `/nodes/{id}` | Update `{name, type, code, description, sortOrder, active, force}` | 200 | 400, 403, 404, 409 |
| PATCH | `/nodes/{id}/move` | `{newParentId}` | 200 | 400, 403, 404, 409 |
| DELETE | `/nodes/{id}` | Delete | 204 | 403, 404, 409 |
| PUT | `/nodes/{id}/members/{memberId}` | Place a member | 200 | 403, 404 |
| DELETE | `/nodes/{id}/members/{memberId}` | Remove placement | 200 | 403, 404 |
| POST | `/import` | multipart `file` (CSV) → `{created, failed, errors:[{row, message}]}` | 200 | 400, 403 |
| GET | `/levels` | Ordered level types | 200 | 403 |
| PUT | `/levels` | Replace ordered list `{levels:[{type, label?}]}` | 200 | 400, 403, 409 |

Error codes: 400 invalid input or level order, 409 duplicate name, node has children/members, or type in use. Node: `{id, parentId, name, type, code, description, sortOrder, active, childCount, memberCount, createdAt, updatedAt}`.
OpenAPI contract: not maintained separately; the controller and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
