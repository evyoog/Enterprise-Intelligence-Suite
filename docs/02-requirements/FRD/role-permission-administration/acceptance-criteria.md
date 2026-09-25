# Acceptance criteria — Role and Permission Administration

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a platform administrator **When** they create a PLATFORM role with a new name and some permissions **Then** it appears in the roles list with those permissions and a `ROLE_CREATED` audit record exists | TC-<APP-CODE>-<NNN> (not created) |
| AC-2 | **Given** a platform administrator **When** they create an ORGANIZATION role named anything other than `ORG_ADMIN` or `MEMBER` **Then** the response is 400 and the UI shows the backend message | TC-<APP-CODE>-<NNN> (not created) |
| AC-3 | **Given** an existing role name **When** a role with the same name is created **Then** the response is 409 and the UI shows the backend message | TC-<APP-CODE>-<NNN> (not created) |
| AC-4 | **Given** a role **When** its description or permissions are edited **Then** the change is saved, and its name and scope stay unchanged | TC-<APP-CODE>-<NNN> (not created) |
| AC-5 | **Given** a system-managed role (`ADMIN`, `ORG_ADMIN`, `MEMBER`) **When** delete is attempted **Then** the response is 403 and the role remains | TC-<APP-CODE>-<NNN> (not created) |
| AC-6 | **Given** a platform administrator **When** they create a permission with a new name **Then** it appears in the permissions list with `roleCount` 0 | TC-<APP-CODE>-<NNN> (not created) |
| AC-7 | **Given** a permission granted by at least one role **When** delete is attempted **Then** the response is 400 naming those roles | TC-<APP-CODE>-<NNN> (not created) |
| AC-8 | **Given** a system-managed permission **When** delete is attempted **Then** the response is 403 | TC-<APP-CODE>-<NNN> (not created) |
| AC-9 | **Given** a user without `MANAGE_ROLES` **When** they call `/admin/roles` **Then** the response is 403 | TC-<APP-CODE>-<NNN> (not created) |
| AC-10 | **Given** the role and permission pages are shown **Then** their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations | TC-<APP-CODE>-<NNN> (not created) |
