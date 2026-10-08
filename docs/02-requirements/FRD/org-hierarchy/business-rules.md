# Business rules — Organization hierarchy

| ID | Rule |
|---|---|
| BR-ORG-001 | The tree belongs to one organization. Every read and write is limited to the caller's organization; a node of another organization is "not found". |
| BR-ORG-002 | Only holders of `MANAGE_ORGANIZATION` can read or change the tree. |
| BR-ORG-003 | One root per organization (type = the first level, Organization). The root cannot be moved, deactivated, deleted or retyped; it can be renamed and described. |
| BR-ORG-004 | Sibling names are unique, ignoring case and surrounding spaces. |
| BR-ORG-005 | Level order: `rank(child) > rank(parent)` (lower rank = higher in the tree). Applies on create, retype, move (to the node and every descendant) and import. |
| BR-ORG-006 | A node type must be one of the organization's level types, stored upper-case with underscores (`BUSINESS_UNIT`). Defaults: ORGANIZATION 0, DIVISION 1, BUSINESS_UNIT 2, DEPARTMENT 3, LOCATION 4, COST_CENTER 5, TEAM 6. |
| BR-ORG-007 | A level type used by any node cannot be removed. Reordering is refused if it would break BR-ORG-005 for an existing parent and child. |
| BR-ORG-008 | A node cannot be its own parent or move under a descendant (no cycles). |
| BR-ORG-009 | Every successful move appends one history row (previous parent, new parent, who, when). History is never edited. |
| BR-ORG-010 | Deactivating a node that has children or placed members requires explicit confirmation; otherwise it is refused with a message that names what is attached. |
| BR-ORG-011 | A node with children or placed members cannot be deleted. |
| BR-ORG-012 | A member has at most one home node and must belong to the same organization as the node. |
| BR-ORG-013 | CSV import uses the same rules as manual creation (BR-ORG-003 to 006); one bad row never stops the others. `parentName` must match a node that already exists or an earlier row; if the name is ambiguous the row fails. |
| BR-ORG-014 | The hierarchy grants no permission and changes no subscription, product access or group by itself. |
| BR-ORG-015 | Every change is audited (actions `ORG_NODE_*`, `ORG_LEVELS_CHANGED`). |
