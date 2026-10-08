# Screens: Offerings

**Requirement:** [REQ-CAT-005](../../02-requirements/FRD/offering-management/requirement.md)

1. **Admin — Offerings** (`/admin/offerings`, Platform area, `MANAGE_CATALOG`). Header button *New offering*. A note says an offering has no price of its own. Tab *Offerings*: table of name, status chip, products, edit and (drafts only) delete. Tab *Product rules*: table of product, who can buy, works-with chips, edit.
2. **Offering dialog.** Name, description, status, a checklist of products (inactive ones are marked). Save is disabled until there is a name and at least one product. The server's message is shown on refusal.
3. **Product rule dialog.** Who can buy (radio: individuals and organizations / individuals only / organizations only); a note that the required product is set on the product as its dependency; a checklist of the other products it works with.
4. **Public — Offerings** (`/offerings`, reached from the Product catalog header). Cards with name, description and product chips. Empty state when none. **Offering detail** (`/offerings/:id`): the included products, each linking to its own page; a note that each product is priced and bought on its own page.
5. **Product page — Works with.** In the Overview tab, chips linking to the other products; hidden when the list is empty.
6. **Cart.** An item's issue line reads *<Product> is not available for your account type.*
