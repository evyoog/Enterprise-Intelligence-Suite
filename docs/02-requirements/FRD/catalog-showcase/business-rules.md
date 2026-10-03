# Business rules — Catalog Showcase

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-CAT-301 | A platform appears in the public catalog only when its status is ACTIVE **and** "show in catalog" is on. Otherwise the catalog details endpoint returns 404. | backend (`CatalogService`) | REQ-CAT-003.1, .2 |
| BR-CAT-302 | Catalog app counts, categories and feature tags are derived from the platform's **ACTIVE** apps only, at request time. Nothing is stored or entered by hand. | backend (`CatalogService`) | REQ-CAT-003.1 |
| BR-CAT-303 | Catalog order: display order ascending, then name (case-insensitive). Display order is 0–9999, default 0. | backend, database CHECK | REQ-CAT-003.3 |
| BR-CAT-304 | Colours are `#RRGGBB`. Stored upper-case. Empty means "default" (platform) or "inherit" (app). | backend `@Pattern`, database CHECK, frontend | REQ-CAT-003.3, .4 |
| BR-CAT-305 | App colour resolution: the app's accent colour, else the first assigned platform with a colour, else #6366F1. | frontend (`appColor`) | REQ-CAT-003.4 |
| BR-CAT-306 | Feature tags: at most 12, each 1–40 characters, no commas, trimmed, duplicates (case-insensitive) dropped. Cards show the first 3 (platform cards the first 6 from the catalog API) plus "+N". | backend, frontend | REQ-CAT-003.5 |
| BR-CAT-307 | Documentation and support URLs must start with `http://` or `https://`; blank means none. | backend `@Pattern`, frontend | REQ-CAT-003.6 |
| BR-CAT-308 | Accent colour is used only for icons, badges, borders, hover and links on a white card; text drawn in the colour is darkened until it reaches 4.5:1 on white. | frontend (`readableOnWhite`) | REQ-CAT-003.9 |
| BR-CAT-309 | Showing data the backend does not have is not allowed: no invented versions, ratings, counts or features. Absent values are omitted, not filled. | frontend | REQ-CAT-003.10 |
| BR-CAT-310 | The existing price rule is unchanged: the flat price is shown only if the app has no pricing tiers. | frontend | REQ-CAT-002 |

Retire still keeps BR rules of [product-lifecycle](../product-lifecycle/business-rules.md): saving a retired app through the form keeps it RETIRED.
