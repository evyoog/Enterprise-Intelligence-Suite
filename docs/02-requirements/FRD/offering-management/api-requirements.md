# API requirements — Offering management

| Method and path | Who | Purpose |
|---|---|---|
| `GET /offerings` | public | ACTIVE offerings with their ACTIVE products |
| `GET /offerings/{id}` | public | One ACTIVE offering; 404 otherwise |
| `GET /products/{id}/works-with` | public | ACTIVE products the product works with; 404 for an unknown product |
| `GET /admin/offerings` | `MANAGE_CATALOG` | Every offering, every status |
| `GET /admin/offerings/{id}` | `MANAGE_CATALOG` | One offering |
| `POST /admin/offerings` | `MANAGE_CATALOG` | Create (`name`, `description`, `status`, `productIds`) |
| `PUT /admin/offerings/{id}` | `MANAGE_CATALOG` | Replace |
| `DELETE /admin/offerings/{id}` | `MANAGE_CATALOG` | Delete a DRAFT offering (204) |
| `GET /admin/offerings/product-rules` | `MANAGE_CATALOG` | Audience and works-with of every product |
| `PUT /admin/offerings/product-rules/{productId}` | `MANAGE_CATALOG` | Set `audience` (`BOTH`, `INDIVIDUAL`, `ORGANIZATION`) and `worksWithProductIds` |

Errors: 400 invalid input, 404 not found, 409 name already used or not allowed in the current state (publish without an active product, delete a non-draft offering, subscribe or order a product that is not for the buyer type). Cart validation adds the issue code `NOT_ELIGIBLE` to `GET /me/cart/validate` and checkout.
