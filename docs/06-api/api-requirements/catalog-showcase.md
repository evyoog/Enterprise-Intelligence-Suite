# API — Catalog showcase (C66)

FRD: [REQ-CAT-003](../../02-requirements/FRD/catalog-showcase/requirement.md). Controller: `modules/platform/controller/CatalogController`. All changes are additive; no existing field was renamed or removed.

## GET `/api/catalog/platforms` — public
Visible platforms (BR-CAT-301), sorted by `displayOrder`, then name. `categories` and `featureTags` are derived from ACTIVE apps; `featureTags` limited to 6 on this list.

```json
[
  { "id": 1, "name": "Thiran", "description": "…", "imageUrl": "/api/platforms/images/…", "primaryColor": "#7C3AED",
    "appCount": 2, "categories": ["Analytics", "Planning"], "featureTags": ["Reports", "Dashboard", "Gantt"] }
]
```

## GET `/api/catalog/platforms/{id}` — public
Same fields with **all** feature tags plus `apps` (ACTIVE `ProductDto`s). 404 when the platform does not exist, is INACTIVE or is not shown in the catalog. `null` fields are omitted.

## Changed requests (existing endpoints, `MANAGE_CATALOG`)
| Endpoint | New optional fields | Validation (400 when broken) |
|---|---|---|
| `POST /api/platforms`, `PUT /api/platforms/{id}` | `primaryColor`, `status` (`ACTIVE`/`INACTIVE`, default ACTIVE), `showInCatalog` (default true), `displayOrder` (0–9999, default 0) | `#RRGGBB` |
| `POST /api/products`, `PUT /api/products/{id}` | `accentColor`, `featureTags` (≤ 12 × ≤ 40, no commas), `documentationUrl`, `supportUrl` | `#RRGGBB`; `https?://…` |

## Changed responses
- `PlatformDto`: + `primaryColor`, `status`, `showInCatalog`, `displayOrder`.
- `PlatformSummaryDto` (inside `ProductDto.platforms`): + `primaryColor`, `imageUrl`.
- `ProductDto`: + `accentColor`, `featureTags` (array, empty when none), `documentationUrl`, `supportUrl`.

## Security
`SecurityConfig` permits anonymous `GET /catalog/platforms` and `GET /catalog/platforms/*`; everything else is unchanged.

## C94 additions (public catalog redesign)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/products/ratings` | The rating of every product that has an APPROVED review: `[{ productId, averageRating (one decimal), reviewCount }]`. Products without an approved review are left out. Public, like the product list | 200 | |

The product create and update request and the product response gain `bannerImageUrl` (C95; the URL returned by `POST /products/images`, up to 500 characters; blank = none). `imageUrl` stays the app icon. The product response also gains `createdAt` (ISO time the product was added), used for the New badge and the Newest sort. Nothing else changes.

