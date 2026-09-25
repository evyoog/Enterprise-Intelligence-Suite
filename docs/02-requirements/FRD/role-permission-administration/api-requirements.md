# API requirements — Role and Permission Administration

All endpoints **already exist** on branch `dev`. Paths are relative to the backend base path `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/admin/roles` | List roles | `MANAGE_ROLES` | 200 `RoleDto[]` | 403 |
| GET | `/admin/roles/{id}` | Get one role | `MANAGE_ROLES` | 200 `RoleDto` | 403, 404 |
| POST | `/admin/roles` | Create a role | `MANAGE_ROLES` | 201 `RoleDto` | 400, 403, 409 |
| PUT | `/admin/roles/{id}` | Update description / permissions | `MANAGE_ROLES` | 200 `RoleDto` | 400, 403, 404 |
| DELETE | `/admin/roles/{id}` | Delete a role | `MANAGE_ROLES` | 204 | 403, 404 |
| GET | `/admin/permissions` | List permissions | `MANAGE_PERMISSIONS` | 200 `PermissionDto[]` | 403 |
| POST | `/admin/permissions` | Create a permission | `MANAGE_PERMISSIONS` | 201 `PermissionDto` | 400, 403, 409 |
| PUT | `/admin/permissions/{id}` | Update description | `MANAGE_PERMISSIONS` | 200 `PermissionDto` | 403, 404 |
| DELETE | `/admin/permissions/{id}` | Delete a permission | `MANAGE_PERMISSIONS` | 204 | 400, 403, 404 |

## Request / response
`CreateRoleRequest`: `{ "name": string (required), "scope": "PLATFORM" | "ORGANIZATION" (required), "description": string, "permissionIds": number[] }`

`UpdateRoleRequest`: `{ "description": string, "permissionIds": number[] }`

`RoleDto`: `id`, `name`, `scope`, `description`, `permissionNames` (string[]), `systemManaged`.

`CreatePermissionRequest`: `{ "name": string (required), "description": string }`

`UpdatePermissionRequest`: `{ "description": string }`

`PermissionDto`: `id`, `name`, `description`, `systemManaged`, `roleCount`.

Frontend clients: new `frontend/src/api/rolesApi.ts` and `frontend/src/api/permissionsApi.ts`.

OpenAPI contract: `docs/06-api/openapi/API-<APP-CODE>-<NNN>.yaml` (not created). The running backend serves its live spec at `/api/swagger-ui.html` in dev and uat.
