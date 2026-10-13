# API requirements — Wishlist (module `wishlist`)

Requirement: [REQ-MKT-004](../../02-requirements/FRD/wishlist/requirement.md), decision [C94](../../01-business/roadmap/open-decisions.md#c94). All endpoints need sign-in (the existing authenticated `/me/**` rule) and act on the caller's own list.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/me/wishlist` | The caller's saved product ids, newest saved first | 200 `[3, 1]` | 401 |
| PUT | `/me/wishlist/{productId}` | Save a product (nothing changes when it is already saved); returns the new list | 200 | 401, 404 (unknown or not ACTIVE product) |
| DELETE | `/me/wishlist/{productId}` | Remove a product (nothing changes when it is not saved); returns the new list | 200 | 401 |

The app turns the ids into products with the public `GET /products` list, so the page shows the same data as the catalog.
