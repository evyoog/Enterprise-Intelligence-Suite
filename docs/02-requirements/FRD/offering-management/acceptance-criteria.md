# Acceptance criteria — Offering management

| ID | Criterion |
|---|---|
| AC-1 | An administrator can create an offering with a name and products; it starts as DRAFT and keeps the product order. |
| AC-2 | An offering with no product, an unknown product or a name already used is refused. |
| AC-3 | An offering is published only with an active product; customers see only ACTIVE offerings and their ACTIVE products. |
| AC-4 | Only DRAFT offerings can be deleted; a product in an offering cannot be deleted. |
| AC-5 | Nothing on the offering screens shows a price of its own; the note says prices stay on the products. |
| AC-6 | A product's audience can be set to individuals only, organizations only or both; with no rule it is open to both. |
| AC-7 | An individual cannot check out, subscribe to or order an organization-only product; an organization member cannot buy an individual-only one. Cart shows *not available for your account type*. |
| AC-8 | A product's required product is its dependency; no second place to set it. |
| AC-9 | The *Works with* list is shown on the product page for ACTIVE products and nothing is shown when empty. |
| AC-10 | Changing a rule or an offering is in the audit log; deleting a product removes its rules and works-with rows. |
| AC-11 | A person without `MANAGE_CATALOG` cannot reach the admin endpoints; browsing needs no sign-in. |
| AC-12 | The screens work in English and Spanish and pass the accessibility checks. |
