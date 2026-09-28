# UI requirements — Product Reviews & Ratings

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Product detail | `/products/:id` | Public (signed in or not) | Not specified |
| Reviews moderation | `/admin/reviews` | `MANAGE_REVIEWS` | Not specified |

The product detail page is new this sprint — the catalog's own tiles now link here ("Reviews & ratings") instead of nowhere. It shows the product's description, the average rating (MUI `Rating`, read-only) with review count, every APPROVED review, and — signed in only — a form to submit or edit the caller's own review, prefilled from `GET /me/products/{id}/review` when one already exists.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Rating | Star rating (1–5) | Yes | Must be set before Submit is enabled | - |
| Comment | Text (multiline) | No | - | - |

## States
- Empty: "No reviews yet." (`reviews.noReviews`)
- Submitted: "Your review is pending approval and is not yet shown publicly." (`reviews.yourReview.pendingNotice`) shown immediately after a successful submit
- Error: the backend's own message shown in a dismissible `Alert`
- Admin moderation controls (Approve/Reject) only show for a PENDING review

## Accessibility and localization
- The rating control is a labeled MUI `Rating` (radio group under the hood); the comment field is a labeled `TextField`.
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `reviews.*`.
