# Business rules — Group Management

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-TEN-020 | A group belongs to exactly one organization, set at creation from the caller's own membership — never from a client-supplied organization id. | backend | REQ-TEN-003.1 |
| BR-TEN-021 | Deleting a group deletes its membership rows first, then the group itself (no orphaned `organization_group_member` rows). | backend | REQ-TEN-003.2 |
| BR-TEN-022 | Adding a member already in the group is a no-op — no duplicate `organization_group_member` row, no error. | backend | REQ-TEN-003.5 |
| BR-TEN-023 | Acting on a group or member belonging to a different organization than the caller's own is refused with a 404 ("Group not found" / "Member not found") — same "don't confirm cross-tenant existence" pattern as `requirePermissionOnMember`. | backend | REQ-TEN-003.6 |
| BR-TEN-024 | Every group action (create/delete/add member/remove member) is audited (`GROUP_CREATED`, `GROUP_DELETED`, `GROUP_MEMBER_ADDED`, `GROUP_MEMBER_REMOVED`). | backend | REQ-TEN-003.1–.4 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
