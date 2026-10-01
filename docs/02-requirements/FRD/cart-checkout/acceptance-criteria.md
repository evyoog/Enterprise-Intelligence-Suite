# Acceptance criteria — Cart and checkout (REQ-MKT-003)

Each criterion maps to at least one test case in `test-cases/functional/cart-checkout/` (written in Phase 2).

| ID | Requirement | Criterion | Test cases |
|---|---|---|---|
| AC-1 | .1 | **Given** a signed-in customer on a product with a paid plan **when** they click Buy **then** the product and plan are in their cart, they are on `/cart`, and the toast "Added {product} — {plan} to your cart" is shown. | TC-MKT-0xx (Phase 2) |
| AC-2 | .1 | **Given** a cart with product P on plan Monthly **when** the customer clicks Buy on P's Yearly plan **then** the cart still holds P once, on the Yearly plan (to confirm in review). | Phase 2 |
| AC-3 | .1 | **Given** a free plan **when** the customer clicks its Subscribe button **then** the current free-plan behaviour runs and nothing is added to the cart. | Phase 2 |
| AC-4 | .1 | **Given** a visitor who is not signed in **when** they click Buy **then** they are asked to sign in and nothing is stored for the anonymous visitor (Open question 5). | Phase 2 |
| AC-5 | .2 | **Given** a customer with two items in their cart **when** they sign out and sign in on another device **then** the cart shows the same two items. | Phase 2 |
| AC-6 | .3 | **Given** a cart with items and saved billing details in a region with GST 18 % **then** each item shows image, name, plan, billing period, price and line amount, and the summary shows the subtotal, "GST (18 %)" with its amount, the total and the item count. | Phase 2 |
| AC-7 | .3 | **Given** a customer with no billing details **then** the summary shows "Tax calculated at payment" in place of tax lines. | Phase 2 |
| AC-8 | .4 | **Given** a cart item **when** the customer clicks Remove **then** the item disappears, the totals update, and the toast "Removed {product} · Undo" is shown for about 5 seconds; **when** they click Undo **then** the item is back with the same plan. | Phase 2 |
| AC-9 | .4 | **Given** a cart item **when** the customer changes its plan to another plan of the same product **then** the line, subtotal, tax and total update immediately; **if** the server refuses the change **then** the previous plan and totals are restored and an error is shown. | Phase 2 |
| AC-10 | .4 | **Given** a cart with items **when** the customer clicks Clear cart and confirms **then** the cart is empty; **when** they cancel the confirmation **then** nothing changes. | Phase 2 |
| AC-11 | .4 | **Given** any cart **then** no quantity stepper, coupon field, fee line or shipping line is shown. | Phase 2 |
| AC-12 | .5 a | **Given** an item whose product was retired or set inactive (or whose plan was deleted) after it was added **when** the customer clicks Proceed **then** that item shows "This product is no longer available." (or "This plan is no longer available."), the page scrolls to it, and the customer stays on the cart. | Phase 2 |
| AC-13 | .5 b | **Given** an item whose price changed from 1,000.00 to 1,200.00 after it was added **when** the customer clicks Proceed **then** the item shows "The price changed from 1,000.00 to 1,200.00." with a Confirm new price action, and checkout is blocked until they confirm or remove the item. | Phase 2 |
| AC-14 | .5 c | **Given** an item for a product the customer already has an ACTIVE subscription to **when** they click Proceed **then** the item shows "You already have an active subscription to {product}." and checkout is blocked. | Phase 2 |
| AC-15 | .5 d | **Given** an item for product B, which depends on product A, and the customer neither has A nor has A in the cart **when** they click Proceed **then** the item shows "{B} requires {A}." and checkout is blocked; **given** A is also in the cart **then** there is no issue. | Phase 2 |
| AC-16 | .5 | **Given** the cart passed validation **when** a product is retired before the checkout is created **then** creating the checkout is refused with the same per-item issue, and no invoice or order is created. | Phase 2 |
| AC-17 | .6 | **Given** an empty cart **then** `/cart` shows the icon, "Your cart is empty", "Find a product to get started." and a Browse products button that opens the catalog. | Phase 2 |
| AC-18 | .7 | **Given** a signed-in user with 3 items in their cart **then** every signed-in page shows the cart icon with the badge "3", and clicking it opens `/cart`; **given** an empty cart **then** the icon shows no count. | Phase 2 |
| AC-19 | .8 | **Given** an individual with a valid cart **when** they click Proceed to checkout **then** the subscriptions and their invoice are created, the cart is emptied, and the checkout opens at Billing details. | Phase 2 |
| AC-20 | .8 | **Given** an organization member with a valid cart **when** they click Submit order for approval **then** an order is created under REQ-ORD-001, the cart is emptied, and the Complete step shows "Order submitted for approval"; no invoice exists until an admin approves. | Phase 2 |
| AC-21 | .8, BR-10 | **Given** a valid cart **when** Proceed is clicked twice quickly **then** exactly one checkout (one invoice or one order) is created. | Phase 2 |
| AC-22 | .9 | **Given** a customer adds, changes and removes items **then** no audit entries are written; **when** they submit an order or pay **then** the existing audit entries are written. | Phase 2 |
| AC-23 | Accessibility | **Given** the cart page, the Clear cart confirmation and the toasts **then** each passes the axe test; Remove, Undo and Change plan are keyboard operable; the badge has an accessible name such as "Cart, 3 items". | Phase 2 |
| AC-24 | BR-1 | **Given** user A's cart item ID **when** user B calls PATCH or DELETE on it **then** the response is 404 and A's cart is unchanged. | Phase 2 |
