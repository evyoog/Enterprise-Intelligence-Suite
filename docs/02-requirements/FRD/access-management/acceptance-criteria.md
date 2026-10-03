# Acceptance criteria — Access management (REQ-TEN-005)

Functional test cases are written in `test-cases/functional/access-management/` when the FRD is approved and built. UAT scripts: [test-cases/UAT/access-management](../../../../test-cases/UAT/access-management/README.md).

| ID | Requirement | Criterion |
|---|---|---|
| AC-1 | .1 | **Given** Member role defaults with product A ON **when** a new member joins **then** their effective access includes product A, marked "From role". |
| AC-2 | .1, BR-9 | **Given** a member with an override (product B ON) **when** their role changes from Member to Organization admin and back **then** their access is recalculated from the new role defaults each time and the product B override is kept; each change is in the audit log. |
| AC-3 | .2 | **Given** the Role defaults tab **when** an admin turns *Approve orders* ON for Member and saves **then** the impact message names the number of members without custom access, and those members gain *Approve orders*. |
| AC-4 | .2, BR-6 | **Given** the Organization admin role **then** every feature permission is shown locked ON and cannot be turned off (UI and API refuse). |
| AC-5 | .3 | **Given** a member **when** an admin turns a feature permission ON that the role does not give **then** it shows "Custom" and is enforced; **when** the admin chooses Reset to role default **then** the override is removed and the role default applies again. |
| AC-6 | .3 | **Given** a member with three overrides **when** Reset all to role default is confirmed **then** all three overrides are removed. |
| AC-7 | .4, BR-11 | **Given** default access **when** a Member tries to suspend an organization subscription **then** it is refused (403) and an Organization admin can do it. |
| AC-8 | .4 | **Given** an admin grants *Manage subscriptions* to member M **then** M can suspend, reactivate, cancel, change plan and change seats of organization subscriptions; after the override is reset M cannot. |
| AC-9 | .5, BR-7 | **Given** product C is not in the organization's active subscriptions **then** it is not listed in Product access and a grant through the API is refused. |
| AC-10 | .5, BR-8 | **Given** product access consumes seats (Open question 3) and no seat is free **then** the toggle is disabled with "No free seats on {product}" and the API refuses the grant. |
| AC-11 | .6a, BR-2 | **Given** delegated administrator D without *Approve orders* **when** D tries to grant *Approve orders* to a member **then** the toggle is disabled with the reason and the API refuses it. |
| AC-12 | .6b, BR-4 | **Given** delegated administrator D (not an organization admin) **when** D opens an organization admin's access **then** every control is disabled with "Only organization admins can change an administrator's access" and the API refuses changes. |
| AC-13 | .6c, BR-3 | **Given** any user **when** they open their own access **then** every control is disabled with "You cannot change your own access" and the API refuses changes to their own role or access. |
| AC-14 | .6d, BR-5 | **Given** one active organization admin **when** any request would leave the organization without an active admin (demote, suspend or remove) **then** it is refused with "Cannot remove the organization's last administrator." (existing tests keep passing). |
| AC-15 | .7 | **Given** a member **when** Mark as reviewed is clicked **then** the list and panel show the review date and reviewer. |
| AC-16 | .8, BR-12 | **Given** 10 selected members **when** Grant product X is applied and 2 have no free seat **then** the preview says "Grant Product X to 8 members — 2 skipped: no seats" and only the 8 are changed. |
| AC-17 | .9, BR-13 | **Given** any change **then** the Activity tab and the audit log show actor, member, item, from → to, basis and time. |
| AC-18 | .10 | **Given** an organization with no overrides and role defaults equal to today's role permissions **then** every existing organization permission check behaves as before (existing tests pass). |
| AC-19 | .10 | **Given** an active organization privileged-access grant **then** it still satisfies the permission check, as today. |
| AC-20 | .11 | **Given** the platform Roles & permissions screen **then** a platform admin can create, edit and delete roles and permissions as on the old pages, and protected roles and permissions show the existing validation messages. |
| AC-21 | Accessibility | **Given** the Access management screen (each tab, the member panel, the bulk dialog) and the Roles & permissions screen **then** each passes the axe test; switches are `role="switch"` with labels; disabled reasons are reachable by keyboard. |
