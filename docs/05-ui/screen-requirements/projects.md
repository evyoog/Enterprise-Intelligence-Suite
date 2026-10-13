# Screens: Projects

**Requirement:** [REQ-TEN-009](../../02-requirements/FRD/projects/requirement.md)

1. **Projects tab** (People & structure; visible to `ORG_ADMIN` and to members with `MANAGE_USERS` or `MANAGE_PRODUCT_ACCESS`). Header button *New project* (needs `MANAGE_USERS`). Table: name, description, number of members, number of products. Row actions: *Open*, *Rename* and *Delete* (confirmation that says how many members lose access).
2. **New project / Rename dialog.** Name (required), Description (optional). Server messages (duplicate name) appear in the dialog.
3. **Project panel.** Two sections. *Members*: list with name, email, role and *Remove*; an *Add member* picker of active members not yet in the project. *Products*: list with product name and project role, *Change role* and *Remove*; an *Add product* picker of products the organization holds an active subscription for, plus a role field. Sections the caller may not change are read-only.
4. **Empty states.** "No projects yet. Create one to give a team access to products." and, in a project, "No members yet" and "No products yet".
