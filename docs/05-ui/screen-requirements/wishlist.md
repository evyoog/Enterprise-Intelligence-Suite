# Screen: Wishlist

| Field | Value |
|---|---|
| Requirement | [REQ-MKT-004](../../02-requirements/FRD/wishlist/requirement.md), [C94](../../01-business/roadmap/open-decisions.md#c94) |
| Route | `/my/wishlist` (signed-in users) |
| Entry | **Wishlist (n)** button in the catalog banner; no sidebar entry |
| Built | 2026-10-13 — `frontend/src/pages/WishlistPage.tsx`, `frontend/src/components/wishlist/WishlistContext.tsx` |

## Content
- Page header "Wishlist" with "Products you saved. Subscribe or add them to your cart when you are ready."
- The saved products as `CatalogProductCard`s (3 per row on a wide screen), newest saved first, each with the rating, From price, **Subscribe**, **Add to cart** and a pressed heart that removes it.
- Empty: "Your wishlist is empty", "Tap the heart on a product to save it here." and **Browse products** (→ `/products`).
- States: skeletons; error with Retry. A saved product that is no longer in the catalog is not shown.

## Accessibility and i18n
The heart is a toggle button (`aria-pressed`) named "Save {name} to wishlist" or "Remove {name} from wishlist"; axe test for the page. Strings under `wishlist.*` (English and Spanish).
