# UI requirements — Order Lifecycle

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Orders | `/organization/orders` | Any organization member; ORG_ADMIN additionally sees a pending-approvals section | Not specified |

One page for both roles: the request form and "my requests" list show for every member; the pending-approvals section renders only when the pending-orders call succeeds (a 403 from the backend hides it — the backend, not this page, is the real gate).

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Product | Select | Yes | Must be an ACTIVE product | Backend message shown as-is |
| Plan | Select | No (empty = flat price) | Must belong to the selected product | Backend message shown as-is (BR-ORD-001) |
| Decision note | Text | No | - | - |

## States
- Empty: "You have not requested any products yet." (`orders.noOrders`); "No orders are waiting on your decision." (`orders.pending.none`)
- Loading: handled by each section rendering only once its own data resolves
- Error: the backend's own message shown in a dismissible `Alert`
- Per-row actions are disabled (not hidden) while that row's own action is in flight

## Accessibility and localization
- Every action is a labeled `<button>`; both selects use `InputLabel` + `labelId`.
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `orders.*`.

## Purchase requests from the cart ([C100](../../../01-business/roadmap/open-decisions.md#c100))
- A request made from the cart appears in **My requests** and in **Pending approvals** as "Purchase request #{id}", with **Products in this request** (one line per product and plan) and **"Reason: {reason}"** (`orders.purchaseRequestNumber`, `orders.products`, `orders.reason`). A one-product order without a reason shows as before.
- The cart's dialog **"Submit a purchase request"** (required "Why do you need this?", 500 characters at most) is described in [cart.md](../../../05-ui/screen-requirements/cart.md#purchase-request-dialog).
- The request form on this page still has one product and an optional plan and **no reason field** ([Open points](requirement.md#open-points)).

