# REQ-MKT-004 — Wishlist

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-13 ([C94](../../../01-business/roadmap/open-decisions.md#c94), CG-3)

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) (catalog carry-over) |
| Requirement ID | REQ-MKT-004 |
| Application | [03 Marketplace & Cart](../../../01-business/roadmap/applications/03-marketplace.md) |
| Application code | `APP-MKT` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P1 |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source
The product owner's catalog redesign request of 2026-10-13: the mockup shows a heart on each product card, and the answer to CG-3 asks for it. It has no function ID of its own; it sits next to 03.03 Cart. Decision [C94](../../../01-business/roadmap/open-decisions.md#c94).

## Summary
A signed-in customer saves products with a heart on the catalog cards and finds them again on a Wishlist page, where they can subscribe or add them to the cart.

## Actors
- Signed-in customer or organization member (their own list)
- Visitor (not signed in): the heart sends them to sign in

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-004.1 | **Save and remove:** a signed-in user can save a product with the heart on a catalog card and remove it with the same heart. Saving a product that is already saved changes nothing. The heart says whether the product is saved. | Must |
| REQ-MKT-004.2 | **Active products only:** only an ACTIVE product can be saved; an unknown or not-active product is refused (404). | Must |
| REQ-MKT-004.3 | **Own list:** a user sees and changes only their own list. The list is stored on the server, so it is the same on every device, and is cleared from the screen at sign-out. | Must |
| REQ-MKT-004.4 | **Wishlist page** `/my/wishlist`: the saved products as the catalog's product cards, newest saved first, with Subscribe, Add to cart and the heart (which removes the product from the page). A saved product that is no longer active is not shown. An empty list shows "Your wishlist is empty" and **Browse products**. | Must |
| REQ-MKT-004.5 | **Entry points:** a **Wishlist (n)** button in the catalog banner for signed-in users and a **Saved products only** filter in the catalog filter panel. There is no sidebar entry (the menu stays short, [C80](../../../01-business/roadmap/open-decisions.md#c80)). | Must |
| REQ-MKT-004.6 | **Signed-out visitor:** clicking the heart sends the visitor to sign in and back to the page they were on; nothing is saved before sign-in. | Must |

## Out of scope
- Sharing a wishlist, several lists, notes, price-drop alerts, moving a saved product to the cart in one click for all items.
- A sidebar menu entry.
- Auditing: wishlist changes are shopping activity and are not audited, as for the cart ([REQ-MKT-003.9](../cart-checkout/requirement.md)).

## Dependencies
- The catalog cards ([REQ-CAT-003.13](../catalog-showcase/requirement.md)), the cart ([REQ-MKT-003](../cart-checkout/requirement.md)).
- New module `modules/wishlist`; table `wishlist_item` ([data model](../../../07-database/data-model/wishlist.md), migration [V034](../../../../database/migrations/V034__wishlist.sql)).
- API: [wishlist.md](../../../06-api/api-requirements/wishlist.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [wishlist.md](../../../05-ui/screen-requirements/wishlist.md) |
| API | [api-requirements.md](api-requirements.md) → [wishlist.md](../../../06-api/api-requirements/wishlist.md) |
| Data model | [wishlist.md](../../../07-database/data-model/wishlist.md) |

## Open questions
None blocking. Not specified: wishlist sharing, price alerts, a sidebar entry.
