# UI requirements — Product evaluation (REQ-MKT-005)

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Product page: "Try it first" panel (Start free trial, Request a demo, Watch tutorials) and the demo dialog | `/products/:id` | Everyone sees it; trial and demo need sign-in | [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md#1-product-page-try-it-first-panel) |
| My subscriptions and the organization's subscriptions: "Free trial" chip, days left, Subscribe | `/my/subscriptions` | Signed-in customer; organization section as before | [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md#2-my-subscriptions-and-organization-subscriptions-free-trial) |
| Product form: "Free trial (days)" | `/admin/settings/product`, `/admin/products/:id/edit` | `MANAGE_CATALOG` | [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md#3-admin-product-form-free-trial-days) |
| Catalog: Compare tick on the cards and the Compare tray | `/products` | Everyone | [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md#4-catalog-compare-tick-and-tray) |
| Compare page | `/compare?items=…&seats=…` | Everyone (the tax estimate needs sign-in and billing details) | [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md#5-compare-page) |

Related screens: the product page and the product form are described in [catalog-showcase.md](../../../05-ui/screen-requirements/catalog-showcase.md); My subscriptions in [my-subscriptions.md](../../../05-ui/screen-requirements/my-subscriptions.md); the cart in [cart.md](../../../05-ui/screen-requirements/cart.md).
