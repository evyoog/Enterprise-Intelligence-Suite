# Business rules — Offering management

| ID | Rule |
|---|---|
| BR-OFR-001 | Only `MANAGE_CATALOG` creates, changes, publishes, retires or deletes offerings and sets product rules. Browsing is public. Checked on the server. |
| BR-OFR-002 | An offering has no price. Any price a customer sees comes from a product plan. |
| BR-OFR-003 | An offering needs at least one existing product, and a name that no other offering has (ignoring case). |
| BR-OFR-004 | A product appears at most once in an offering; the order the administrator gives is kept. |
| BR-OFR-005 | ACTIVE requires at least one ACTIVE product. The public view hides products that are not ACTIVE. |
| BR-OFR-006 | Only DRAFT offerings are deleted. A product that is in any offering cannot be deleted. |
| BR-OFR-007 | A product with no eligibility row is open to individuals and organizations. Setting the audience to `BOTH` removes the row. |
| BR-OFR-008 | `ORGANIZATION` products are not sold to individuals; `INDIVIDUAL` products are not sold through an organization. The check cannot be skipped by calling subscribe or order directly. |
| BR-OFR-009 | A product's required product is its dependency (REQ-CAT-001), enforced as before (`MISSING_DEPENDENCY`). |
| BR-OFR-010 | "Works with" is informational: it never blocks a purchase. |
| BR-OFR-011 | Only ACTIVE products are listed publicly as "works with". |
