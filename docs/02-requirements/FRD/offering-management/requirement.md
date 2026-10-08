# REQ-CAT-005 — Offering management

**Status:** Approved (2026-10-08, [C85](../../../01-business/roadmap/open-decisions.md#c85))
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-08 (answers to OF-1 to OF-7; OF-2 and OF-4 explained, not chosen)

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-CAT-005 |
| Application | [02 Product Catalog Management](../../../01-business/roadmap/applications/02-product-catalog-management.md#0202-offering-management) |
| Functions | 02.02.01.01 Create offering, 02.02.01.02 Bundle products, 02.02.01.03 Define prerequisites, 02.02.01.04 Define compatibility, 02.02.02.02 Define channels, 02.02.02.03 Define eligibility. **02.02.02.01 Define regions is not built** (see the open conflict below) |
| Priority | P0 |

## Decisions this FRD is built on
| ID | Answer | Effect |
|---|---|---|
| OF-1 | **A.** A new "Offering" object above products | A new catalog object `offering` that groups products. Purchase is not rewired (see OF-2). |
| OF-2 | **No option chosen.** The platform administrator keeps setting prices | An offering has **no price of its own**. Each product keeps the plans and prices set by the platform administrator. Bundle pricing stays open. |
| OF-3 | **A.** Every product can be bought in every region | No regional availability rule. |
| OF-4 | **No option chosen.** The platform is the sales channel | No channel object. Other channels may come with future products. |
| OF-5 | **B.** A few fixed rules per product: individuals and/or organizations, allowed regions, a required product | Built: audience and required product. **Allowed regions is not built** — it contradicts OF-3 (see below). |
| OF-6 | **A.** Prerequisites are the product dependencies we already built | Reuse `Product.dependsOn` (REQ-CAT-001). No new data. |
| OF-7 | **B.** A simple "works with" list per product, shown on the product page | Table `product_compatibility`; shown in the product's Overview tab. |

## Summary
An administrator groups products into **offerings** that customers can browse, and sets, per product, **who can buy it** (individuals, organizations, or both) and **which other products it works with**. A product's required product is its existing dependency. Offerings do not change how anything is bought or priced: a customer still subscribes to a product, at the plan and price the platform administrator set.

## Actors
- **Platform administrator** (`MANAGE_CATALOG`): creates, edits, publishes, retires and deletes offerings; sets product rules.
- **Customer or visitor**: browses published offerings and the "works with" list; buys products as before.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-CAT-005.1 | **Offering (02.02.01.01, .02):** name (required, up to 150 characters, unique ignoring case), description (up to 1,000), status, and an ordered list of one or more existing products. | Must |
| REQ-CAT-005.2 | **States:** DRAFT (default), ACTIVE, RETIRED. An offering can be ACTIVE only when at least one of its products is ACTIVE. Only a DRAFT offering can be deleted; others are retired. | Must |
| REQ-CAT-005.3 | **Public view:** anyone can list and open ACTIVE offerings; inside them only ACTIVE products are shown. A DRAFT or RETIRED offering answers "not found". | Must |
| REQ-CAT-005.4 | **No price:** an offering has no price, plan or checkout of its own (OF-2). The screens say so. | Must |
| REQ-CAT-005.5 | **Product in use:** a product that belongs to an offering cannot be deleted (it can still be retired). | Must |
| REQ-CAT-005.6 | **Prerequisites (02.02.01.03):** the product's dependencies (REQ-CAT-001) are the prerequisites; nothing new is stored (OF-6). | Must |
| REQ-CAT-005.7 | **Works with (02.02.01.04):** per product, a list of other products it works with. One-directional (A works with B does not make B work with A). A product cannot work with itself. The public product page shows ACTIVE entries; it shows nothing when the list is empty. | Must |
| REQ-CAT-005.8 | **Eligibility (02.02.02.03):** per product, the audience is `BOTH` (default), `INDIVIDUAL` or `ORGANIZATION`. A product with no rule behaves exactly as before. | Must |
| REQ-CAT-005.9 | **Enforcement:** the audience is checked in cart validation (code `NOT_ELIGIBLE`, which blocks checkout like the other issues), in an individual's subscribe (single and from the cart) and in an organization's order submission. The buyer is an organization when the caller is an active organization member, otherwise an individual. | Must |
| REQ-CAT-005.10 | **Channels (02.02.02.02):** the platform is the only channel. No channel data is stored (OF-4). | Must |
| REQ-CAT-005.11 | **Cleanup:** deleting a product removes its rule and every "works with" row that mentions it. | Must |
| REQ-CAT-005.12 | **Audit:** offering created, updated, deleted; product rules changed. | Must |
| REQ-CAT-005.13 | **Screens:** admin page *Offerings* (tabs Offerings and Product rules), public *Offerings* list and detail, a *Works with* section on the product page, a *Not available for your account type* line in the cart. English and Spanish. | Must |

## Not built — open
- **Regions (02.02.02.01).** OF-3 says every product can be bought in every region; OF-5 B lists "allowed regions" among the product rules. Both cannot hold. Nothing regional is built until one is withdrawn (C85).
- **Bundle pricing (OF-2) and buying an offering as a whole.** OF-1 says customers buy offerings. Until bundle pricing is decided, an offering groups and presents products; each product is bought on its own.
- **Sales channels (OF-4)** beyond the platform.
- Offering images, offering-level plans, offering versions, per-offering eligibility, a "required product" that differs from the product's dependency, symmetrical "works with", translated offering text.

## Dependencies
REQ-CAT-001 (products, dependencies), REQ-CAT-002 (plans and prices), REQ-MKT-003 (cart validation), REQ-SUB (subscribe), REQ-ORD (organization orders), audit.
