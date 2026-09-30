# EIS Platform UI/UX Consistency Pass (C44, C45, C48, C49)

| Field | Value |
|---|---|
| Decisions | [C44](../01-business/roadmap/open-decisions.md#c44), [C45](../01-business/roadmap/open-decisions.md#c45), [C48](../01-business/roadmap/open-decisions.md#c48), [C49](../01-business/roadmap/open-decisions.md#c49) |
| Requested | 2026-09-29 (C44), 2026-09-30 (C45, C48, C49) |
| Status | Phases 1–2 done, plus the purchase flow (C48) and the product page's tabbed/interactive redesign (C49) (this doc records both what shipped and what's next) |
| Reference | `evyoog.com` was requested as the visual/brand reference, twice. Neither attempt could reach it from this build environment — the network egress proxy blocks the whole `vyoog.com` domain family, confirmed against the marketing site, the logo image URL the app already loads (`www.vyoog.com/wp-content/uploads/...`), and re-tested for C45. Nothing below claims to match evyoog.com pixel-for-pixel. C45 unified the app's own two existing color systems (the MUI app theme and the public landing page) into one instead — see "Brand" below. |

## Why this doc exists

A platform-wide UI/UX request this large (navigation redesign, screen consolidation, catalog completeness, "feel like one AWS/Salesforce/Zoho-style platform") doesn't fit inside a single FRD the way a feature does — there's no single `REQ-<CODE>-NNN`. It's recorded here instead, linked from decision C44, and updated as later phases land. Follow-up work should update this file's own status table rather than opening a parallel document.

## What shipped in this pass

### 1. Catalog seeding — every named eVyoog product suite is now in the database

**Problem found:** the live catalog only had whichever platforms an admin had manually created (observed: "Thiran" and "Thittam", one app each) — not the full set of six suites this platform is actually for (`docs/01-business/vision.md`).

**Fix:** `backend/src/main/java/com/vyoog/eisplatform/modules/product/service/CatalogSeeder.java` — a new idempotent `ApplicationRunner` (same pattern as `RbacSeeder`/`PlatformAdministrationSeeder`), seeds all six suites — Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai — as a `Platform` each, with one flagship `Product` (`ACTIVE`, `featured`, one `Standard` plan at $0/month, since no billing model exists — see decision C38). Runs every backend startup; checks by name first, so re-running it (including against the shared environment once this code deploys there) never creates duplicates.

**Honesty note on content:** this repository's own source documents (`docs/01-business/vision.md`, `scope.md`, `EIS-document-analysis.md`) name the six suites but describe none of their actual features — "named only." Since `evyoog.com` couldn't be reached, three suites (Thittam.ai, Yukth.ai, Tharav.ai) got a plain, honest placeholder description rather than invented marketing copy. Three (Valam.ai, Varthan.ai, Thiran.ai) got a short real description sourced from this codebase's own existing test fixtures and analysis doc (not fabricated). `launchUrl` and `imageUrl` are left blank for all six — this environment could not verify a single real URL or logo asset, and shipping an unverified guess is worse than shipping nothing. **Follow-up:** once real copy/images/URLs are available, update them through the existing admin Catalog UI (`/admin/apps` → edit) — no code change needed.

### 2. Public homepage — already did most of what was asked

**Checked before assuming a gap:** `frontend/src/pages/HomePage.tsx` already pulls every product from the real `GET /products` API (never hardcoded), already lists each with its description in a "Products" section, and already has a pricing section per product with a button that opens the registration ("get started") flow.

**Added:** an explicit **Subscribe** button (not just "Explore") on each product card in the main products section, plus its category shown as a badge — so "a subscription button that goes to the get-started page" is satisfied by name on the card itself, not only implied further down the page.

### 3. Global Search moved into the top bar

This was the explicit worked example in the request ("Global Search should be in the top bar, not a separate screen") and is the template the remaining consolidations below follow.

- **Removed:** the "Search" sidebar nav item (`appNavigation.ts`).
- **Added:** `frontend/src/components/layout/TopBarSearch.tsx` — a debounced search box in the signed-in top bar (`AppShell.tsx`), showing a grouped live-result dropdown (Products / Knowledge base / Your tickets), with a "See all results" row that deep-links to `/search?q=...`.
- **Kept:** the `/search` route itself, and `GlobalSearchPage.tsx` — still real, still reachable, just not a permanent nav destination. On mobile (where a persistent search box doesn't fit the top bar) a search icon button opens `/search` directly instead.

### 4. Create added to the admin Products page (built between C44 and C45, on direct request)

`/admin/apps` had Read (the grid), Update and Delete (`ProductTile`'s edit/trash icons) but not Create — the only way to add a product was the separate Settings page (item #2 below). An "Add App" button now opens the same `ProductForm` in a dialog, reused verbatim; the grid refetches on success via a new `reloadToken` prop on `ProductGrid`. The Settings page still works too (not removed, since removing it wasn't asked for) — it's just no longer the only way in.

### 5. One color system instead of two (C45)

**Problem found:** `theme.ts` (the signed-in MUI app) and `styles/landing.css` (the public marketing page) were never the same brand — `theme.ts` used a generic blue (`#2563eb`, no secondary color at all); `landing.css` already had its own real blue/violet/cyan triad (`--blue #4c63ff`, `--violet #733dff`, `--cyan #42d8ff`). A visitor saw one palette on `/` and a different one the instant they signed in.

**Fix:** `theme.ts`'s `primary`/`secondary`/`info` (light and dark) now derive from that same triad instead of an unrelated blue. A handful of `landing.css` rules that had drifted to their own one-off hex (the product-card icon gradients, the featured-plan button, the auth-modal focus/link colors) now reference the file's own `--blue`/`--violet`/`--cyan` custom properties, the way the rest of that file already did.

### 6. Product descriptions refined (C45)

Thittam.ai, Yukth.ai and Tharav.ai's C44 placeholder ("full product description not yet available") is now a short, professional, generic-but-honest one-liner and category (Planning / Automation / Insights) — no specific feature/integration/certification is claimed, since no source describes what these three suites actually do, but it no longer reads as an apology on a live product page either.

### 7. Subscribe + access-flow showcase on the product page (C45, superseded by C48 below)

`/products/:id` (`ProductDetailPage.tsx`) had ratings/reviews but no way to actually get the product. It now has:
- A **Subscribe** button — C45 had it call `POST /me/subscriptions` directly with an inline success alert; C48 replaced that with a real navigation to the dedicated checkout page (see #8) once Billing & Payments existed to check out into.
- A secondary link to the existing organization-purchase flow (`/organization/orders`) for org-context buying.
- A four-step **"How you get access"** showcase — Discover → Subscribe → Get access → Launch — depicting the platform's own real flow (what subscribing and single sign-on launch actually do here), not a fabricated per-product business process this codebase has no source for.
- A **"What's included"** list (C48), sourced from the plan's own `includedFeatures` field.

### 8. A real purchase flow: checkout page + full-page sign-in (C48)

Subscribe used to be an inline action (C45: call the endpoint, show an alert on the same page) — reasonable before Billing & Payments existed, but with a real invoice/payment step now built (C46), a purchase deserved its own screen:
- **`/checkout/:productId`** — confirm/save billing details (skipped straight through once details are on file) → activate the subscription (unchanged endpoint, still generates the invoice in the same call) → pay the resulting invoice through Razorpay Checkout right there, reusing Billing's own pay-invoice calls. A $0 plan skips straight to a plain confirmation, since no invoice exists to pay.
- **`/login`** — a real page (not the modal) with `?returnTo=`, for exactly one reason: Subscribe while signed out needs to come back to checkout afterward, which a modal staying open over the product page doesn't do as cleanly as a real navigation. The modal (`AuthCredentialsForm`, now shared between both) still handles every in-app "Login" link — this page isn't a replacement for it, just an addition for this one flow. It carries the same "Don't have an account? Create one" link to `/register` the modal already had.
- **Catalog interactivity** — a quick Subscribe action was added directly to product cards on `/products` and the homepage (not just the detail page), routing through the same signed-in/signed-out rule.

### 9. The product page itself, made interactive (C49)

`/products/:id` was one long scroll (header → what's included → access flow → reviews → write-a-review), asked for by name as needing "interactive UI." Reorganized into three tabs — **Overview**, **Pricing**, **Reviews (count)** — and the ratings section gained a real rating-distribution breakdown (5★ down to 1★, computed from this product's own approved reviews) that doubles as a filter: click a bar to narrow the review list to that star value, click again to clear it. The header's average-rating line is itself clickable, jumping straight to the Reviews tab. Nothing here is decorative-only — every interaction reveals or narrows real data this page already had.

## Full audit: what else was reviewed, and the plan for it

Every screen in `frontend/src/pages/` and `frontend/src/pages/admin/` was reviewed against three questions: does it duplicate another screen's data, does it hide a primary action (like "create") somewhere a user wouldn't look for it, and is its table/filter/form pattern consistent with the platform's other admin screens. Findings below; **only #3 above has been built** — the rest are scoped and ready to build next, in the order listed.

### Screens confirmed as genuinely distinct (not touched, not flagged)

Knowledge Base admin, Support Tickets admin, Reviews admin, Service Status admin, Partners admin, Audit Log, Registrations (already a well-built 3-tab pattern — Organizations / Individuals / Pending), Identity Federation (already merges SAML + OIDC config on one screen). Each has its own real data model and workflow; merging any of these would lose information, not simplify it.

### Consolidations planned but not yet built

| # | Today | Planned | Why |
|---|---|---|---|
| 1 | Security page + Preferences page (2 sidebar items) | One **Account** screen, tabbed (Security, Preferences, Privileged Access) | Every reference platform in the request (AWS, Salesforce, Zoho) treats "my account" as one settings surface, not several |
| 2 | Settings → "Add app" form page, Settings → "Add platform" form page | Removed as pages; become a "+ Create" dialog opened from the Apps list and Platforms list themselves | Creating a record is a primary action on the list it creates into, not a hidden Settings sub-page — this is the same "don't create a separate screen for every function" principle applied to creation, not just search |
| 3 | Platforms list (`/admin`) + flat Apps list (`/admin/apps`) as two sidebar items | One **Catalog** screen with a "By platform / All apps" view toggle | Same underlying data, two lenses — Salesforce's list-view picker does exactly this |
| 4 | Platform Dashboard (`/admin/platforms/:id`) as a separate top-level page | Folded into Catalog as a drill-in state (URL kept for deep-linking) | It's Catalog filtered to one platform, not a different screen |
| 5 | Roles page + Permissions page + Privileged Access page (3 sidebar items) | One **Access Control** screen, 3 tabs | Textbook AWS IAM / Salesforce Setup pattern |
| 6 | My Products + My Subscriptions (2 sidebar items) | One **My Products** screen, subscription controls (suspend/renew/change plan) inline per row | A customer thinks "my stuff," not two separate categories of it |

### Systemic consistency work (affects every admin list screen)

Right now each admin list (Reviews, Partners, Tickets, Audit Log, Registrations…) hand-builds its own table, filter and pagination, so they're all subtly different. Planned: a shared `<DataTable>` (consistent header/sort/pagination/empty-state), `<FilterBar>` (consistent search + status chips + date range), and `<FormDrawer>` (consistent slide-over create/edit), then retrofit every admin list onto them. Two concrete gaps this audit found along the way: **Reviews admin has no status filter** (lists every review, no way to narrow to PENDING) and **Partners admin has no status filter** either — both get one for free once `<FilterBar>` lands.

### Brand

`evyoog.com` was requested as the brand/color reference twice (C44, C45) and both attempts confirmed it's unreachable from this build environment — the network egress proxy blocks the whole `vyoog.com` domain family, including the in-app logo image URL. Guessing colors and presenting them as "the eVyoog brand" would be worse than not touching the palette, so C44 left `theme.ts` alone.

C45 took a different, honest path: instead of inventing a third palette, it unified the app's own two pre-existing, already-in-repo color systems. `styles/landing.css` (the public marketing page) already had a real blue/violet/cyan triad (`--blue #4c63ff`, `--violet #733dff`, `--cyan #42d8ff`); `theme.ts` (the signed-in MUI app) used an unrelated generic blue with no secondary color at all. `theme.ts`'s `primary`/`secondary`/`info` tokens (light and dark) now derive from that same triad, so the signed-in app and the public site are one consistent brand instead of two. The in-app logo (`https://www.vyoog.com/.../evyoog-logonew1.png`, loaded from the user's own browser, not from this build environment) is kept as-is everywhere it already appears. If real brand tokens (hex values, type, logo files) become available from evyoog.com, they drop into `theme.ts`'s existing token structure directly — no structural change needed first.

## Suggested build order for what's left

1. Shared `<DataTable>` / `<FilterBar>` / `<FormDrawer>` components (everything else below builds on these).
2. Account merge (Security + Preferences) — smallest, self-contained.
3. Access Control merge (Roles + Permissions + Privileged Access).
4. Catalog merge (Platforms + Apps + Platform Dashboard) + move create-actions out of Settings.
5. My Products merge (+ My Subscriptions).
6. Retrofit the remaining admin list screens (Reviews, Partners, Tickets, Audit Log, Registrations, Service Status, Knowledge Base) onto `<DataTable>`/`<FilterBar>`, adding the two missing status filters found above.
