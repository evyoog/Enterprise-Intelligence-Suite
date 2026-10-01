# UI requirements — Cart and checkout (REQ-MKT-003)

UI requirements for this feature live in **docs/05-ui/screen-requirements/**, one file per screen. This file is the index.

| Screen | Route | Roles | Screen requirement |
|---|---|---|---|
| Cart | `/cart` | Any signed-in customer or organization member | [cart.md](../../../05-ui/screen-requirements/cart.md) |
| Checkout (Cart › Billing details › Payment › Complete) — shared with REQ-BIL-001.18 | `/checkout?…` | Owner of the checkout | [checkout-payment.md](../../../05-ui/screen-requirements/checkout-payment.md) |
| Cart icon and badge in the top bar | every signed-in page | Signed-in users | [application-layout.md](../../../05-ui/screen-requirements/application-layout.md) |
| Payment logos and icons (used by the cart and the checkout) | — | — | [payment-brand-assets.md](../../../05-ui/screen-requirements/payment-brand-assets.md) |

Shared presentation rules: [billing-ui-standards.md](../../../05-ui/screen-requirements/billing-ui-standards.md). Colours come from the unified MUI theme ([C45](../../../01-business/roadmap/open-decisions.md#c45)), in light and dark mode. Related functions are grouped into one screen with steps ([C44](../../../01-business/roadmap/open-decisions.md#c44)).

Wireframes: Not specified. The two reference screenshots supplied with the request are described in the screen files. They are not stored in the repository.
