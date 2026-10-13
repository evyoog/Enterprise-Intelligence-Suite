# Acceptance criteria — REQ-TEN-009 Projects

| ID | Criterion |
|---|---|
| AC-1 | An administrator can create a project with a name and description; a second project with the same name (ignoring case) is refused. |
| AC-2 | Active members of the organization can be added to and removed from a project; a member of another organization or an inactive member cannot. |
| AC-3 | A product with an active organization subscription can be added with a role; one without a subscription, or an inactive product, is refused. |
| AC-4 | Every project member gets that product as product access with the project's role, and loses it again when removed, when the product is removed or when the project is deleted. |
| AC-5 | A direct assignment is never changed by a project; a direct revoke of project access is refused with the project's name. |
| AC-6 | A member in two projects that give the same product keeps access when one source goes away. |
| AC-7 | A member who gains access through a project is notified once. |
| AC-8 | Groups lists only plain groups; a project cannot be changed through the Groups endpoints. |
| AC-9 | Permissions are enforced: `MANAGE_USERS` for members, `MANAGE_PRODUCT_ACCESS` for products, another organization's project is not found. |
| AC-10 | Every change is in the audit log; the screens work in English and Spanish and pass the accessibility checks. |
