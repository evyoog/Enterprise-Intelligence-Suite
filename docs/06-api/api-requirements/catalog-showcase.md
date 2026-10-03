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
