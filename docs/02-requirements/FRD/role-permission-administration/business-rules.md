# Business rules — Role and Permission Administration

These rules are **already enforced by the backend**. They are documented here from the code, not newly defined. The cited class and method are the source of truth. `BR-IAM-<NNN>` numbers are placeholders until rule IDs are assigned.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-<NNN>.1 | `/admin/roles/**` requires the platform permission `MANAGE_ROLES`. `/admin/permissions/**` requires `MANAGE_PERMISSIONS`. Both are seeded for the platform `ADMIN` role. | backend | `SecurityConfig` (request matchers), `RbacSeeder` |
| BR-IAM-<NNN>.2 | A platform permission is also satisfied by an active, approved PLATFORM-scope privileged-access grant for it. | backend | `PermissionAuthorizationManagerFactory` → `PrivilegedAccessService#hasActivePlatformGrant` |
| BR-IAM-<NNN>.3 | Role names are unique (after trimming). A duplicate returns 409 "A role named '<name>' already exists". | backend | `RoleAdminService#createRole` |
| BR-IAM-<NNN>.4 | An ORGANIZATION-scope role name must be `ORG_ADMIN` or `MEMBER`; any other name returns 400 with an explanation. A PLATFORM-scope role may have any name. | backend | `RoleAdminService#requireAssignableName` |
| BR-IAM-<NNN>.5 | A role's name and scope cannot be changed after creation. Only its description and permission set can be edited. On update, omitting `permissionIds` keeps the current permissions. | backend | `UpdateRoleRequest` javadoc, `RoleAdminService#updateRole` |
| BR-IAM-<NNN>.6 | Every permission id given for a role must exist. Otherwise 400 "One or more permission ids do not exist". | backend | `RoleAdminService#resolvePermissions` |
| BR-IAM-<NNN>.7 | The roles `ADMIN`, `ORG_ADMIN` and `MEMBER` are system-managed (`systemManaged: true`) and cannot be deleted: 403 "The '<name>' role is required by the platform and cannot be deleted". | backend | `RoleAdminService#deleteRole`, `PROTECTED_ROLE_NAMES` |
| BR-IAM-<NNN>.8 | Permission names are unique (after trimming). A duplicate returns 409 "A permission named '<name>' already exists". | backend | `PermissionAdminService#createPermission` |
| BR-IAM-<NNN>.9 | A permission's name cannot be changed after creation. Only its description can be edited. | backend | `UpdatePermissionRequest` javadoc, `PermissionAdminService#updatePermission` |
| BR-IAM-<NNN>.10 | The permissions `MANAGE_CATALOG`, `MANAGE_REGISTRATIONS`, `MANAGE_PRIVILEGED_ACCESS`, `VIEW_AUDIT_LOG`, `MANAGE_ORGANIZATION`, `MANAGE_USERS`, `MANAGE_PRODUCT_ACCESS`, `MANAGE_ROLES` and `MANAGE_PERMISSIONS` are system-managed and cannot be deleted (403). | backend | `PermissionAdminService#deletePermission`, `PROTECTED_PERMISSION_NAMES` |
| BR-IAM-<NNN>.11 | A permission still granted by any role cannot be deleted: 400 naming those roles ("remove it from those roles first"). | backend | `PermissionAdminService#deletePermission` |
| BR-IAM-<NNN>.12 | An unknown role or permission id returns 404. | backend | `RoleAdminService#findRoleOrThrow`, `PermissionAdminService#findOrThrow` |
| BR-IAM-<NNN>.13 | Every create, update and delete is audited: `ROLE_CREATED`, `ROLE_UPDATED`, `ROLE_DELETED`, `PERMISSION_CREATED`, `PERMISSION_UPDATED`, `PERMISSION_DELETED`. | backend | `RoleAdminService`, `PermissionAdminService` → `AuditService#recordSuccess` |
