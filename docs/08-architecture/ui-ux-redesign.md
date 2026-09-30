# EIS Platform UI/UX Consistency Pass (C44)

| Field | Value |
|---|---|
| Decision | [C44](../01-business/roadmap/open-decisions.md#c44) |
| Requested | 2026-09-29 |
| Status | Phase 1 done (this doc records both what shipped and what's next) |
| Reference | `evyoog.com` was requested as the visual/brand reference. It could not be reached from this build environment — the network egress proxy blocks the whole `vyoog.com` domain family, confirmed against both the marketing site and the logo image URL the app already loads (`www.vyoog.com/wp-content/uploads/...`). Nothing below claims to match evyoog.com pixel-for-pixel; the existing in-app logo and a professional enterprise-platform palette were used instead — see "Brand" below. |

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

`theme.ts` was not changed in this pass — the real brand reference (`evyoog.com`) was unreachable, and guessing colors and presenting them as "the eVyoog brand" would be worse than leaving the current palette in place. The in-app logo (`https://www.vyoog.com/.../evyoog-logonew1.png`, loaded from the user's own browser, not from this build environment) is kept as-is everywhere it already appears. If real brand tokens (hex values, type, logo files) become available, they drop into `theme.ts`'s existing token structure directly — no structural change needed first.

## Suggested build order for what's left

1. Shared `<DataTable>` / `<FilterBar>` / `<FormDrawer>` components (everything else below builds on these).
2. Account merge (Security + Preferences) — smallest, self-contained.
3. Access Control merge (Roles + Permissions + Privileged Access).
4. Catalog merge (Platforms + Apps + Platform Dashboard) + move create-actions out of Settings.
5. My Products merge (+ My Subscriptions).
6. Retrofit the remaining admin list screens (Reviews, Partners, Tickets, Audit Log, Registrations, Service Status, Knowledge Base) onto `<DataTable>`/`<FilterBar>`, adding the two missing status filters found above.
