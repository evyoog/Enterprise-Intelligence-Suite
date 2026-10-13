# Acceptance criteria — Wishlist (REQ-MKT-004)

| ID | Requirement | Criterion | Test case |
|---|---|---|---|
| AC-1 | .1 | **Given** a signed-in user **when** they save a product twice **then** it is in the list once; the list is newest first. | [TC-MKT-031](../../../../test-cases/functional/wishlist/TC-MKT-031.md) |
| AC-2 | .1 | **Given** saved products **when** one is removed (also one that is not saved) **then** the others stay and no error occurs. | [TC-MKT-032](../../../../test-cases/functional/wishlist/TC-MKT-032.md) |
| AC-3 | .2, .3 | **Given** two users **then** each sees only their own list; saving a retired or unknown product is refused with 404. | [TC-MKT-033](../../../../test-cases/functional/wishlist/TC-MKT-033.md) |
| AC-4 | .3, .6 | **Given** no sign-in **when** the wishlist endpoints are called **then** 401. | [TC-MKT-036](../../../../test-cases/functional/wishlist/TC-MKT-036.md) |
| AC-5 | .1, .5, .6 | **Given** the catalog **when** a signed-in user clicks the heart **then** the product is saved, the heart is pressed and the Wishlist button shows the count; **Saved products only** shows it. A signed-out visitor is sent to sign in and back. | [TC-CAT-057](../../../../test-cases/functional/catalog-showcase/TC-CAT-057.md) |
| AC-6 | .4 | **Given** saved products **then** the Wishlist page shows them newest first, leaves out one no longer in the catalog, removes one with the heart and shows the empty state with Browse products. | [TC-MKT-037](../../../../test-cases/functional/wishlist/TC-MKT-037.md), [TC-MKT-038](../../../../test-cases/functional/wishlist/TC-MKT-038.md) |
| AC-7 | Accessibility | **Given** the Wishlist page and the heart **then** they pass the axe test and the heart announces whether the product is saved. | [TC-MKT-039](../../../../test-cases/functional/wishlist/TC-MKT-039.md) |
