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

## Sidebar (C80)
Requested by the product owner on 2026-10-06 and approved with the mapping in [C80](../../01-business/roadmap/open-decisions.md#c80). **One concept, one primary location**: every item is declared once in `frontend/src/components/layout/navConfig.ts` (label, icon, route, permission rule, group). `buildAppNavigation` (`appNavigation.ts`) filters it by `useAuth().isAdmin` and `GET /me/permissions`, so members, organization administrators and intermediate roles (billing manager, support agent, auditor, knowledge contributor) get exactly the items their permissions allow. Rules check permissions, never role names. Hiding an item is UX only; route guards and the backend stay the enforcement. A group with no visible item is not shown.

| Group | Items (permission) |
|---|---|
| **Workspace** | Dashboard (`VIEW_PLATFORM_DASHBOARD` → `/admin/dashboard`, or organization `MANAGE_ORGANIZATION` → business dashboard; members have none), My applications (customer), Product catalog (everyone: *discover*), Knowledge Center (one entry), Orders (members; see Organization) |
| **Organization** | Members (`MANAGE_ORGANIZATION`), Roles & permissions (`MANAGE_ROLES` / `MANAGE_PERMISSIONS`), Registrations (`MANAGE_REGISTRATIONS`), Privileged access (platform or organization `MANAGE_PRIVILEGED_ACCESS`), Sign-in security (`MANAGE_ORGANIZATION`), Orders (`MANAGE_ORDERS`) |
| **Platform** | Products (`MANAGE_CATALOG`: *manage*), Applications (`MANAGE_CATALOG`), Integrations → Platform events, API keys (`MANAGE_INTEGRATIONS`), Search (`MANAGE_SEARCH`), Service status (platform administrators and organization administrators; once) |
| **Operations** | Billing (`MANAGE_BILLING`, or organization `MANAGE_ORGANIZATION`) → Overview, Invoices & payments (organization) or Invoices & payments, Payment gateway, Billing settings (platform); Support (`MANAGE_SUPPORT_TICKETS` → agent queue; organization administrators → own tickets); Reviews (`MANAGE_REVIEWS`); Partners (`MANAGE_PARTNERS`); Audit log (`VIEW_AUDIT_LOG`) |
| **Help** | Support, for members (same entry as in Operations; never both) |
| **Billing** | Overview, Invoices & payments, for members (same entry as in Operations) |
| **Account** | Security, Preferences (everyone) |

- **Knowledge Center** is the only knowledge entry; the label opens `/knowledge`, the chevron beside it folds the children. Readers: Articles, Guides, FAQs. People holding `KNOWLEDGE_CONTRIBUTE` or `MANAGE_KNOWLEDGE_BASE`: Articles, Drafts, Manage (and Categories for `MANAGE_KNOWLEDGE_BASE`), which use the existing `/knowledge-management/*` routes. Knowledge Management is no longer a sidebar entry.
- **Not in the sidebar:** Search (the top-bar search stays; `/search` is its "See all results" page), Become a partner (a call-to-action; `/partners/apply` stays), Settings.
- **Nested groups** (Billing, Integrations, Knowledge Center): chevron, only the group holding the current page opens by itself (deep links and refresh included), the choice is kept while the page stays open, the current child has `aria-current="page"`. Items sharing a path (Overview / Invoices & payments, Articles / Drafts) are told apart by the query string.
- **Hints:** Product catalog ("Discover products and applications") and Products ("Manage products and platform configuration") have a tooltip so *discover* and *manage* are not confused.
- **Responsive:** one hierarchy at every size. Desktop (≥ 1200 px): permanent sidebar. Tablet (900–1199 px): beside the page, folded away with the menu button. Mobile (< 900 px): drawer.
- **Breadcrumbs** (`NavBreadcrumbs`) show Group / Parent / Page for nested pages (for example Platform / Integrations / API keys, Platform / Products / Edit product).
- Sign out is at the bottom.

### Where the old items went
| Before | Now |
|---|---|
| Service status (every user) and Admin service status | One **Service status** entry; people with `MANAGE_SERVICE_STATUS` get a **Manage** tab (`/status?tab=manage`; `/admin/service-status` redirects) |
| Roles, Permissions | **Roles & permissions** with tabs Roles / Permissions / Role assignments (`/admin/roles`; `/admin/permissions` redirects). Role assignment itself happens per member on **Members** (the tab explains and links) |
| Knowledge Center + Knowledge Management | One **Knowledge Center** entry |
| Become a partner + Partners | **Partners** (operations); the application CTA stays at `/partners/apply` |
| Search | Removed from the sidebar |
| Settings → App | **Applications** (Add dialog; `/admin/settings/product` redirects to `/admin/apps?new=1`) |
| Settings → Products | **Products** (Add product page `/admin/platforms/new`; `/admin/settings/platform` redirects) |
| Settings → Common (languages, currencies, regions, feature flags) | **Products → Configuration** tab (`/admin?tab=configuration`; `/admin/settings/common` redirects) |
| Organization settings (members, groups, MFA policy, privileged access on one page) | **Members** (tabs Members / Groups), **Sign-in security** (tabs Identity federation / Multi-factor policy), **Privileged access**. `/organization/settings` and its `#members`, `#groups`, `#mfa`, `#privileged-access` anchors redirect |
| Billing (three separate entries) | One **Billing** entry per user; the billing tab (`?tab=`) is in the URL |
| Support (two entries) | One **Support** entry; members see it under Help, administrators under Operations |

The redirect table is `components/routing/legacyRedirects.ts`.

## States and accessibility
- Desktop: permanent 260px sidebar. Below the `md` breakpoint it becomes a drawer, opened from the top bar.
- A skip link goes to `#main-content`. The sidebar is a `nav` landmark labelled "Application", and the current page has `aria-current="page"`.
- Text is under `appShell` in `en.json` and `es.json`.
- Tests: `AppShell.test.tsx` (including jest-axe), `appNavigation.test.ts` (member, organization administrator, platform administrator and intermediate roles) and `SidebarConsolidation.test.tsx` (redirects and tab pages). Test cases [TC-PRT-035 to TC-PRT-038](../../../test-cases/functional/application-layout/).
