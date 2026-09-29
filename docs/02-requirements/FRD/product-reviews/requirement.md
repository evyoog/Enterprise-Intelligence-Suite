# REQ-MKT-002 — Product Reviews & Ratings

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C41](../../../01-business/roadmap/open-decisions.md#c41))

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) |
| Requirement ID | REQ-MKT-002 |
| Application | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md) |
| Application code | `APP-MKT` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 03.04.01 | Submit review; Rate product; Moderate review; View ratings | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md#0304-reviews--ratings) |

03.02 Product Evaluation and 03.04's own Comparison-adjacent scope are **not** covered by this requirement — see [C41](../../../01-business/roadmap/open-decisions.md#c41).

## Summary
Any customer can rate a product (1–5) with an optional comment; a platform admin (`MANAGE_REVIEWS`) approves or rejects it before it ever appears publicly or counts toward the average. A new customer-facing product detail page hosts this — none existed before this sprint.

## Actors
- Any authenticated customer — submits/edits their own review
- Any visitor (signed in or not) — reads approved reviews and the average rating
- Platform administrator (`MANAGE_REVIEWS`) — moderates

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-002.1 | An authenticated customer can submit a rating (1–5) and optional comment for an existing product. | Must |
| REQ-MKT-002.2 | Submitting again for the same product edits the customer's existing review rather than creating a second one, and always resets it to PENDING (even if it was previously APPROVED). | Must |
| REQ-MKT-002.3 | A new review is always PENDING; it is never shown publicly or counted in the average until an admin approves it. | Must |
| REQ-MKT-002.4 | An admin can approve or reject a PENDING review; deciding an already-decided review is refused. | Must |
| REQ-MKT-002.5 | Anyone can read a product's APPROVED reviews and the average rating across them; a product with no APPROVED reviews shows a null average and a zero count. | Must |

## Out of scope
- Any AI-assisted moderation or sentiment analysis.
- A cached/denormalized average-rating column on `Product` — the average is computed live from APPROVED reviews on every read, consistent with every other roll-up number in this codebase (seat usage, launch counts, facet counts).
- Editing or deleting another customer's review, or replying to a review.
- Reopening a REJECTED review other than by submitting a fresh one (which is, by design, the same upsert as any other edit).

## Dependencies
- Existing `Product`, `ProductRepository`.
- New table `product_review` ([V011](../../../../database/migrations/V011__product_reviews.sql)).
- New permission `MANAGE_REVIEWS` (platform ADMIN).
- New customer-facing route `/products/:id` (no product detail page existed before this sprint).
