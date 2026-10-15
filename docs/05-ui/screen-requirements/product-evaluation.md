# Screens: Product evaluation

| Field | Value |
|---|---|
| Requirement | [REQ-MKT-005](../../02-requirements/FRD/product-evaluation/requirement.md) (.2, .3, .9 to .15); business rules [BR-EVL-001 to BR-EVL-028](../../02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../01-business/roadmap/open-decisions.md#c102) (TR-1 note, TR-2 A) |
| Built | Sprint 2027.1.3 (built 2026-10-15): `frontend/src/components/evaluation/TryItFirstPanel.tsx`, `DemoRequestDialog.tsx`; `frontend/src/pages/ProductDetailPage.tsx` (the panel); `frontend/src/components/subscriptions/TrialNotice.tsx` (chip and Subscribe), `frontend/src/pages/MySubscriptionsPage.tsx`, `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.tsx`; `frontend/src/components/admin/ProductForm.tsx` (the trial field); `frontend/src/components/catalog/CatalogProductCard.tsx`, `CompareContext.tsx`, `CompareTray.tsx`, `frontend/src/pages/ProductsPage.tsx` (the tick and tray); `frontend/src/pages/ComparePage.tsx`; API clients `frontend/src/api/evaluationApi.ts`, `compareApi.ts` |

Shared presentation rules: unified MUI theme, light and dark mode; states are text, never colour alone. Strings are under `evaluation.*` and `compare.*` in `frontend/src/i18n/locales/{en,es}.json`.

## 1. Product page: "Try it first" panel

| Field | Value |
|---|---|
| Route | `/products/:id` (public), the panel sits above the tabs of the product page ([catalog-showcase.md](catalog-showcase.md#3-app-details--productsid-public)) |
| Permission | Everyone sees it. Starting a trial and requesting a demo need sign-in: a signed-out visitor is sent to sign in and returns to this page |

Header **"Try it first"** — "See whether {product} fits before you subscribe." Three ways to evaluate:

| Element | Shown when | Behaviour |
|---|---|---|
| Offer text | The product sets trial days | "{n}-day free trial, no card needed, all features" |
| **Start free trial** | The product sets trial days | Signed in: starts the trial, for the organization when the customer buys for one (an administrator or a person with Manage billing; the line "The trial will be for your organization." shows), else for the individual (`POST /me/trials`, `POST /organization/me/trials`). Signed out: goes to sign in, with the line "You will be asked to sign in first." On success a status alert "Your free trial of {product} has started and ends on {date}. The product is being set up for you." with **View my subscriptions** (`/my/subscriptions`). A server refusal is shown in an alert (fallback "Could not start the free trial. Please try again.") |
| Trial disabled, with the reason | The customer already has or had a subscription: "You already have, or had, a subscription to this product, so the free trial is not available. You can subscribe at any time." (organization: "Your organization already has, or had, …"); an ordinary organization member: "Only an organization administrator or a person with Manage billing can start a free trial for your organization. Ask one of them." | The button is disabled and the reason sits under it |
| **Request a demo** | Always | Signed in: opens the dialog below. Signed out: sign in first |
| **Watch tutorials** | The product has published videos | Opens the Resources tab on its videos. Hidden when there are none |

A product with no trial days shows no trial offer and no Start button; Request a demo (and Watch tutorials) remain.

### Request a demo dialog
Title "Request a demo of {product}" — "Tell us what suits you. We open a support ticket and the team gets back to you."

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| Preferred time | Text | No | At most 1000 characters | "Keep this under 1000 characters." (hint: "For example, weekdays after 2 pm IST") |
| Message (optional) | Multi-line text | No | At most 1000 characters | "Keep this under 1000 characters." (hint: "What would you like to see?") |

**Send request** (disabled while sending or over the limit), **Cancel**. Success: "Demo requested" — "We opened ticket #{id}. The team will contact you to agree a time." with **View my tickets** (`/support/tickets`) and **Close**. A refusal shows an alert and keeps the form ("Could not send your demo request. Please try again." as a fallback). The next opening starts with an empty form.

## 2. My subscriptions and organization subscriptions: free trial

| Field | Value |
|---|---|
| Route | **My subscriptions**, `/my/subscriptions` ([my-subscriptions.md](my-subscriptions.md)); the Organization subscriptions section on the same page |
| Permission | Any signed-in customer for their own subscriptions; the organization section as before |

A subscription with `trial` true shows, instead of its renewal controls:
- A **"Free trial"** chip and a chip **"Ends {date} ({n} days left)"** ("1 day left" in the singular; whole days, a part of a day counts as a day); once the trial has ended (status EXPIRED, or the time has passed) **"Ended {date}"**.
- The note "A free trial cannot be renewed or changed. Subscribe to keep using {product}." and a **Subscribe** button (accessible name "Subscribe to {product}") that opens the product page `/products/{productId}`, where the normal Buy and cart turn the trial into a paid subscription.
- **No** auto-renew switch, no "Renews on" chip, no **Renew**, no **Change plan**; in the organization section no seat controls. **Suspend** and **Cancel** stay as for any subscription.

Known gap ([BR-EVL-016](../../02-requirements/FRD/product-evaluation/business-rules.md)): while the trial is **running**, adding the product to the cart is refused by the cart's check (`ALREADY_SUBSCRIBED`), so Subscribe only completes a purchase once the trial has **ended**.

## 3. Admin product form: Free trial (days)

| Field | Value |
|---|---|
| Route | `/admin/settings/product` (create) and `/admin/products/:id/edit` (edit), [catalog-showcase.md](catalog-showcase.md#7-create--edit-app--adminsettingsproduct-adminproductsidedit) |
| Permission | `MANAGE_CATALOG` |

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| Free trial (days) | Number, in the product form | No | Empty, or a whole number from 1 to 90 | "Enter a whole number of days from 1 to 90." (hint: "Leave empty to offer no free trial") |

Saved as `trialDays` (a number, or null when empty) through the existing product save. The edit form shows the saved value; emptying the field and saving removes the trial.

## 4. Catalog: Compare tick and tray

| Field | Value |
|---|---|
| Route | `/products` (public), [catalog-showcase.md](catalog-showcase.md#1-product-catalog--products-public--redesigned-by-c94) |
| Permission | Everyone |

- Each product card has a **Compare** tick (a checkbox, accessible name "Compare {product}"). The cards of the form preview and of the wishlist have none.
- At most **4** products can be ticked; once 4 are chosen the other ticks are disabled, with the hint "You can compare up to 4 products."
- The choice is kept for the browser tab (`sessionStorage`, key `eis.compare.items`) and survives a broken stored value.
- Once a card is ticked a **Compare tray** (a labelled region fixed at the bottom of the page) shows "Compare ({n})", one chip per chosen product with a remove button ("Remove {product} from the comparison"), **Clear** and **Compare now**. With fewer than 2 chosen it says "Choose at least 2 products" and Compare now is disabled. **Compare now** opens `/compare?items=…`.

## 5. Compare page

| Field | Value |
|---|---|
| Route | `/compare?items=<productId>[:<planId>],…&seats=<n>` (public; the page shows the site navigation bar when it is not inside the signed-in frame) |
| Permission | Everyone; the tax estimate needs sign-in and billing details |

The address is the state, so a comparison can be shared and bookmarked. Header **"Compare"** — "Put 2 to 4 products or plans side by side and estimate the cost: price × seats + tax."

**Choosing**

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| Add a product | Search picker over the published products | — | At most 4 items | "You can compare up to 4 items. Remove one to add another." |
| Plan (one per chosen item) | Select: "Entry plan (lowest price)" or one of the product's plans | No | — | A product already chosen on its own can be added again with another of its plans |
| Seats | Number | Yes (default 1) | Whole number from 1 to 100 000 | "Seats must be a whole number between 1 and 100000." (hint: "How many seats the subscription is for. Prices are per seat.") |

Each chosen item has a remove button ("Remove {product}"). Changing seats, plans or items updates the address and asks the API again.

**The table** (caption "Comparison of the chosen products and plans"; one column per item, a sticky first column): product name linking to its page with its logo or initials and, when the product offers one, a **"{n}-day free trial"** badge; then the rows **Category, Plan** ("No plan (flat price)"), **Billing period** (Monthly, Yearly, One-time), **Price per seat, Seats, Subtotal, Estimated tax, Estimated total** (with "before tax" under the subtotal), the **Included features** group (a check mark with the hidden text "Included", or "Not included", for every feature of any item) and **Actions**: **View product** and **Add to cart** (disabled, with a tooltip, for a free or missing plan; a signed-out visitor is sent to sign in first). Amounts are in each item's own currency.

| Tax status | Shown in the tax row |
|---|---|
| `ESTIMATED` | The amount, and the total in the Estimated total row |
| `NOT_SIGNED_IN` | A link "Sign in to see the tax estimate" that goes to sign in and back to the comparison |
| `NEEDS_BILLING_DETAILS` | A link to Billing "Add your billing details to see the tax estimate" |
| `AT_CHECKOUT` | "Tax is worked out at checkout" |

Under the table: "Estimates only. Final tax and totals are shown at checkout."

**States:** fewer than 2 items: empty state "Choose at least 2 products to compare" with **Browse the catalog** (the API is not called); loading: a skeleton (`aria-busy`); error: the server's message (for example an unknown product) with **Retry**; invalid seats in the address: an error and no request. A live region announces "Comparison updated: {n} items for {seats} seats."

## Accessibility and i18n
The panel is a labelled section; the dialog is labelled by its title and every field has a visible label with its error as helper text; the trial notes are tied to the button with `aria-describedby`; the Compare tick has the product in its accessible name; the tray is a labelled region with a polite live count; the compare table has a caption, column and row headers and a keyboard-focusable scroll region; the feature marks have hidden text. The panel with the dialog, the compare page and the tray pass the axe test. All strings are in `en.json` and `es.json`.

## API used
[product-evaluation.md](../../06-api/api-requirements/product-evaluation.md): `POST /me/trials`, `POST /organization/me/trials`, `POST /me/demo-requests`, `GET /products/compare`, and `trialDays` on the product create and update.
