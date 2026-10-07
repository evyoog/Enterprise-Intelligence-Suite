# Acceptance criteria — Organization hierarchy

| ID | Criterion |
|---|---|
| AC-1 | Opening the structure the first time creates a root named after the organization and the seven default levels. |
| AC-2 | A child can be created under a node only on a lower level; a same-level or higher-level child is refused. |
| AC-3 | A sibling with the same name (any case) is refused. |
| AC-4 | A node cannot be moved under itself or a descendant; a valid move writes one history row. |
| AC-5 | Deactivating a node with children or members needs confirmation; deleting such a node is refused. |
| AC-6 | The root cannot be moved, deleted, deactivated or retyped. |
| AC-7 | A member of the same organization can be placed on a node and removed; a member of another organization is "not found". |
| AC-8 | CSV import creates valid rows and reports each failed row with its number and reason. |
| AC-9 | Level types can be added, renamed, reordered; a type in use cannot be removed. |
| AC-10 | A user without `MANAGE_ORGANIZATION` gets 403; another organization's node gets 404. |
| AC-11 | Every change appears in the audit log. |
| AC-12 | The screen works in English and Spanish, by keyboard, and passes the accessibility checks. |
