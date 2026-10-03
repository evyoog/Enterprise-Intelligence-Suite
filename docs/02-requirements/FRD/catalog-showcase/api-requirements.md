# API requirements — Catalog Showcase

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/api/catalog/platforms` | Visible platforms with derived counts, categories and tags | Public | 200 | — |
| GET | `/api/catalog/platforms/{id}` | One visible platform with its active apps | Public | 200 | 404 |
| POST / PUT | `/api/platforms`, `/api/platforms/{id}` | Existing; now also accept `primaryColor`, `status`, `showInCatalog`, `displayOrder` | `MANAGE_CATALOG` | 201 / 200 | 400, 403 |
| POST / PUT | `/api/products`, `/api/products/{id}` | Existing; now also accept `accentColor`, `featureTags`, `documentationUrl`, `supportUrl` | `MANAGE_CATALOG` | 201 / 200 | 400, 403 |

Full request and response shapes: [catalog-showcase.md](../../../06-api/api-requirements/catalog-showcase.md).
