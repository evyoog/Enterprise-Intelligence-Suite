# Business rules — Wishlist (REQ-MKT-004)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | A wishlist belongs to one customer; there is no way to address another customer's list. A product appears at most once per customer (unique customer and product). | backend | .1, .3 |
| BR-2 | Only an ACTIVE product can be saved; otherwise 404 and nothing is stored. | backend | .2 |
| BR-3 | Saving a saved product and removing a product that is not saved both succeed and change nothing. | backend | .1 |
| BR-4 | The list is returned newest saved first. A saved product that stops being ACTIVE stays stored but is not shown on the page. | backend, frontend | .4 |
| BR-5 | The list is kept per sign-in: when the user signs out the saved ids are dropped from the screen, and an answer that arrives after sign-out is ignored. | frontend | .3 |
| BR-6 | Wishlist changes are not written to the audit log. | backend | out of scope |
