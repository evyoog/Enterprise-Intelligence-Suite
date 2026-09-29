# Business rules — Product Reviews & Ratings

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-MKT-005 | Submitting a review requires an existing product (404 otherwise). Rating must be an integer 1–5 (validated at the request level). | backend | REQ-MKT-002.1 |
| BR-MKT-006 | At most one review row per (product, customer) — a second submission for the same product finds and edits the existing row (`findByProductIdAndCustomerId`), enforced additionally by a unique DB index. | backend | REQ-MKT-002.2 |
| BR-MKT-007 | Every submit/edit sets `status = PENDING` unconditionally, regardless of the row's previous status. | backend | REQ-MKT-002.2, .3 |
| BR-MKT-008 | Approve/reject requires the review's current status to be PENDING; deciding a non-PENDING review is refused (400). | backend | REQ-MKT-002.4 |
| BR-MKT-009 | The average rating and review count (`getRatings`) are computed only from reviews with `status = APPROVED`; a product with none returns `averageRating: null`, `reviewCount: 0`. | backend | REQ-MKT-002.5 |
| BR-MKT-010 | Every submission and moderation decision is recorded in the audit log (`REVIEW_SUBMITTED`/`_APPROVED`/`_REJECTED`). | backend | REQ-MKT-002.1, .4 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
