# Application layout: public website vs signed-in tool

Requested by the product owner on 2026-09-26. The frontend has two separate frames, chosen by whether the user is signed in.

| State | Frame | Navigation |
|---|---|---|
| Not signed in | **Public Vyoog website** | `SiteNavbar` (Home, Products mega-menu, Solutions, Pricing, Company, Resources, Login, Get started) |
| Signed in | **Vyoog software tool** (`AppShell`) | Sidebar filtered by role and permissions; slim top bar with search, **Home** (website landing page, [C79](../../01-business/roadmap/open-decisions.md#c79)), the cart icon, notifications, theme and the account menu. No website header or marketing copy |

## Routes
| Route | Visitor | Signed-in user |
|---|---|---|
| `/` | Website home | Website home when opened from **Home** or the website logo (C79). Opened any other way (typed address, after sign-in, SSO return): redirected to the tool home: `/admin` for a platform admin, otherwise `/organization/business-dashboard` (which sends members without `MANAGE_ORGANIZATION` and individuals on to `/my/products`) |
| `/products` | Public catalog with the website header and banner | Catalog inside the tool: title, search and filters only |
| `/account/preferences` | Public page with the website header | Inside the tool |
| `/my/products`, `/organization/business-dashboard`, `/account/security`, `/organization/identity-federation` | Redirected to `/` (`RequireAuth`) | Inside the tool |
| `/cart`, `/checkout` | Redirected to sign in (`RequireAuth`; REQ-MKT-003 Open question 5) | Inside the tool ([cart.md](cart.md), [checkout-payment.md](checkout-payment.md)) |
| `/admin/**` | Redirected to `/` (`RequireAdmin`) | Inside the tool for a platform admin; a non-admin is sent to `/products` |
| `/register/**`, `/forgot-password`, `/reset-password` | Public pages | Public pages. The website header then shows only "My Workspace" / "Admin Panel" and Sign out |

## Home button and sign out
Added by [C79](../../01-business/roadmap/open-decisions.md#c79) on 2026-10-06.

- **Home** is in the signed-in top bar (a labelled button on desktop, a house icon with the accessible name "Home" below the `md` breakpoint), first in the website header's links, and first in the website's mobile menu. It opens `/`; on the landing page itself it scrolls back to the top.
- A signed-in user who chooses Home sees the website with its own header ("My Workspace" / "Admin Panel", Sign out). The choice travels as router state (`WEBSITE_HOME_STATE`), so reloading the landing page keeps it; opening `/` without it still goes to the tool.
- **Sign out** anywhere (sidebar, account menu, website header, mobile menu) ends the session (`POST /auth/logout`) and opens `/` (`useSignOut`).
- A session that ends any other way (signed out in another tab or in PMS, expired) also opens `/` (`SignedOutRedirect`).
- **No customer data after sign-out:** the page showing it is replaced by the website home page; a session check already in flight cannot restore the old session, and checks wait for the server-side logout; recent knowledge searches kept in this browser are removed; a late cart count is dropped; API answers are sent with `Cache-Control: no-store`. Theme, language, region and time zone are device settings and stay.
- Text: `nav.home` in `en.json` ("Home") and `es.json` ("Inicio").
- Tests: [TC-PRT-032](../../../test-cases/functional/application-layout/TC-PRT-032.md), [TC-PRT-033](../../../test-cases/functional/application-layout/TC-PRT-033.md), [TC-PRT-034](../../../test-cases/functional/application-layout/TC-PRT-034.md).

## Top bar: cart icon
Added by [C59](../../01-business/roadmap/open-decisions.md#c59) ([REQ-MKT-003.7](../../02-requirements/FRD/cart-checkout/requirement.md)), Draft.

- Every signed-in page shows a **cart icon** in the top bar, left of notifications, with an **item-count badge** (hidden when the cart is empty; "99+" above 99).
- Clicking it opens [`/cart`](cart.md).
- Accessible name: "Cart, {n} items" ("Cart, empty" when there are none); the badge number is not announced separately.
- The count comes from `GET /me/cart` and updates after every add, remove, Undo or checkout without a page reload.
- Whether platform admins see it too: Not specified (proposed: every signed-in user).

## Sidebar
Built by `buildAppNavigation` (`frontend/src/components/layout/appNavigation.ts`) from `useAuth().isAdmin` and `GET /me/permissions`. Each item is gated on the permission its backend endpoints check. This is UI filtering only; the backend stays the security boundary.

| Section | Item | Shown when |
|---|---|---|
| Workspace | Business dashboard | Organization permission `MANAGE_ORGANIZATION` (while permissions load: any non-admin) |
| Workspace | My products | Not a platform admin, or a member of an organization |
| Workspace | Product catalog | Always |
| Organization | Identity federation | Organization permission `MANAGE_ORGANIZATION` |
| Administration | Platforms, All apps, Settings (App / Products / Common) | Platform admin with `MANAGE_CATALOG` |
| Administration | Registrations | Platform admin with `MANAGE_REGISTRATIONS` |
| Administration | Privileged access | Platform admin with `MANAGE_PRIVILEGED_ACCESS` |
| Administration | Roles / Permissions | Platform admin with `MANAGE_ROLES` / `MANAGE_PERMISSIONS` |
| Administration | Audit log | Platform admin with `VIEW_AUDIT_LOG` |
| Account | Security, Preferences | Always |

While `GET /me/permissions` is loading, or if it fails, a platform admin sees the full admin menu (the behavior of the old `AdminLayout`), and organization-only items stay hidden.

## States and accessibility
- Desktop: permanent 260px sidebar. Below the `md` breakpoint it becomes a drawer, opened from the top bar.
- A skip link goes to `#main-content`. The sidebar is a `nav` landmark labelled "Application", and the current page has `aria-current="page"`.
- Text is under `appShell` in `en.json` and `es.json`.
- Tests: `AppShell.test.tsx` (including jest-axe) and `appNavigation.test.ts`.
