# Catalog showcase screens (C66)

FRD: [REQ-CAT-003](../../02-requirements/FRD/catalog-showcase/requirement.md). Design: [design-system.md](design-system.md). All data comes from existing APIs plus `/catalog/platforms`; nothing is hard-coded.

## 1. Product Catalog — `/products` (public)
- Header: label "Product Catalog", title, description; **Add Product** (→ `/admin/settings/platform`) for administrators only.
- Summary cards: Products (= visible platforms), Applications (with category count), Categories, Featured — all counted.
- Filter bar: category chips from the search facets (with counts, `aria-pressed`), Sort (name, price), search with recent searches.
- **Products** section: one `ShowcaseCard` per visible platform → `/catalog/platforms/:id`. Type "Platform", "N apps", top features, "Available".
- **Applications** section: app cards (`ProductTile`) with category badge, features, plan prices (or flat price when there are no tiers), Launch (new tab), Subscribe, View details.
- States: skeletons; empty with Clear filters; error with Retry. `SupportCTA` at the bottom.
- Not shown: versions, ratings (no data).

## 2. Platform details — `/catalog/platforms/:id` (public)
Breadcrumbs, logo, name, Available badge, Overview, Applications grid, Features (all tags), Product information (type, status, apps, categories), `SupportCTA`. Hidden/inactive → error state "not found".

## 3. App details — `/products/:id` (public)
Breadcrumbs (catalog → platform → app), logo, accent from `appColor`, feature tags, Documentation / Support site links when set, existing pricing, subscribe and reviews sections unchanged, `SupportCTA`.

## 4. Platforms — `/admin/platforms`
Same `ShowcaseCard` per platform in display order with the real app count and visibility status (In catalog / Hidden from catalog / Inactive); action "Manage apps" → `/admin/platforms/:id`; Edit → `/admin/platforms/:id/edit`; Add Product.

## 5. Create / Edit Platform — `/admin/settings/platform`, `/admin/platforms/:id/edit`
Sections: **Basic information** (name, description, logo upload/remove), **Appearance** (`ColorPicker`, default #6366F1), **Catalog settings** (status, display order, show in catalog). Sticky save bar (Cancel, Save). **Showcase Preview** aside (sticky on desktop, below on mobile) with the real app count when editing. Validation after the first submit. Create navigates to the new platform's dashboard.

## 6. All Apps — `/admin/apps`
Summary cards (total, SSO connected, standalone, featured). Filter bar: All / Active / Inactive / Retired / Featured (with counts), platform select, search, Table/Cards toggle (cards only on narrow screens). Table: App (logo, name → details, featured star, category), Platform, Status, SSO, Price ("From …" with several tiers), Actions (edit, delete). Delete → `ConfirmDialog`. **Add App** opens the app form in a large dialog (full-screen on phones), keeping C44's create-from-this-page.

## 7. Create / Edit App — `/admin/settings/product`, `/admin/products/:id/edit`
Sections, in order:
1. **Basic information** — name, description, category, logo.
2. **Appearance** — inherit platform colour (default) or custom.
3. **Launch & availability** — launch URL, Active switch (retired apps show a note instead), Featured.
4. **Integration** — "Connected via Vyoog SSO" with the unchanged hint: *Turn this on once the app's own backend has the SSO bridge wired up — it just controls the badge shown on this app's card, it doesn't configure anything itself.*
5. **Product relationships** (was "Product structure") — platforms, parent product, variant label, depends on.
6. **Features** — tag input (Enter or Add), removable chips.
7. **Pricing** — price (*Shown only if no pricing tiers are added.*), tiers with every existing field.
8. **Resources** — documentation URL, support URL.

No **Versions** section: there is no release data. The edit page header shows the status badge, "Revision N" (the edit counter, REQ-CAT-001.1) with an explanatory tooltip, Publish, and Retire (with confirmation). Create navigates to the new app's edit page with a success message.

## Not redesigned
Dashboard, registrations, privileged access, roles, permissions, audit log, service status, knowledge base, support, reviews, partners, billing and the Settings screens use the new theme, shell and page header but keep their existing layouts ([C66](../../01-business/roadmap/open-decisions.md#c66)).
