# Business rules — Projects

| ID | Rule |
|---|---|
| BR-PRJ-001 | A project is an `organization_group` row of kind `PROJECT`. Its name is unique among the organization's projects, ignoring case. Groups and projects may share a name. |
| BR-PRJ-002 | Only active members of the same organization can be added. A member of another organization is answered as "not found". |
| BR-PRJ-003 | A product can be added only when it is Active in the catalog and the organization holds an Active subscription for it. A product role is required (up to 50 characters), a plain string like a direct grant. |
| BR-PRJ-004 | Effective access is derived: for each Active member and each project product, one access row exists with `source_group_id` = the project. If the member already has an Active direct row (source empty), that row is left as it is. |
| BR-PRJ-005 | When a source goes away (member removed, product removed, project deleted), the member's access is recomputed: another project still giving the product keeps the row (with that project's role and source); otherwise the row becomes INACTIVE and its source is cleared. History is kept, as for direct revokes. |
| BR-PRJ-006 | An administrator's direct assignment sets the source to empty (the access is now direct). A direct revoke of an Active row that has a source is refused: "This access comes from project <name>. Remove the member from the project or the product from the project." |
| BR-PRJ-007 | Managing members needs `MANAGE_USERS`; managing products needs `MANAGE_PRODUCT_ACCESS`; viewing needs either. An organization in good standing and Active lifecycle is required, as everywhere else. |
| BR-PRJ-008 | A member newly gaining a product through a project is notified once per gain. A role change or an unchanged recompute sends nothing. A failed notice never fails the change. |
| BR-PRJ-009 | Access follows the project, not the subscription: if the organization's subscription to a product later ends, existing access rows are handled as direct rows are today (REQ-TEN-005 and REQ-SUB-001 own that). |
| BR-PRJ-010 | Deleting a project is allowed at any time and is audited with the number of members and products it had. |
