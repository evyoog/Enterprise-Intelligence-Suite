# Acceptance criteria — Member Role Assignment

Each criterion maps to at least one test case in `test-cases/functional/<feature>/` once the FRD is Approved.

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a caller with `MANAGE_USERS` **When** they open the member list **Then** every member of their organization is shown with name, email, role and status | TC-<APP-CODE>-<NNN> (not created) |
| AC-2 | **Given** a caller with `MANAGE_USERS` **When** they change a `MEMBER` to `ORG_ADMIN` **Then** the row shows `ORG_ADMIN`, the member is notified and a `MEMBER_ROLE_CHANGED` audit record exists | TC-<APP-CODE>-<NNN> (not created) |
| AC-3 | **Given** the organization has exactly one active `ORG_ADMIN` **When** that member is changed to `MEMBER` **Then** the response is 400 "Cannot remove the organization's last administrator." and the UI shows that message | TC-<APP-CODE>-<NNN> (not created) |
| AC-4 | **Given** a member id from another organization **When** the caller changes its role **Then** the response is 403 "You do not have permission to do this" | TC-<APP-CODE>-<NNN> (not created) |
| AC-5 | **Given** a caller without `MANAGE_USERS` **When** they open the member list **Then** the response is 403 and the UI handles it like `BusinessDashboardPage` | TC-<APP-CODE>-<NNN> (not created) |
| AC-6 | **Given** the member list is shown **Then** its text exists in `en.json` and `es.json`, the role selector is keyboard operable, and a jest-axe check reports no violations | TC-<APP-CODE>-<NNN> (not created) |
