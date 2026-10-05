# Knowledge Center — inventory and reuse plan (Phase 0)

Inspected on branch `dev` at `2f351a1` (2026-10-05), before any Knowledge Center change. Facts only; anything not found is stated as missing.

## 1. User Knowledge Base page and routes
| Item | Path |
|---|---|
| Page | `frontend/src/pages/KnowledgeBasePage.tsx` (67 lines): search box (250 ms debounce) and a list of article titles; selecting one shows title and plain-text body (`white-space: pre-wrap`). No multimedia, no categories, no product link |
| Route | `/knowledge-base` (public) in `frontend/src/App.tsx` |
| Navigation | Workspace item `knowledgeBase` → `/knowledge-base` (`components/layout/appNavigation.ts`, line 66) |
| API client | `frontend/src/api/knowledgeBaseApi.ts`: `GET /knowledge-base/articles?q=`, `GET /knowledge-base/articles/{id}` |
| Tests | `frontend/src/pages/KnowledgeBasePage.test.tsx` |
| i18n | `knowledgeBase.*` in `frontend/src/i18n/locales/{en,es}.json` |

## 2. Platform admin knowledge management
| Item | Path |
|---|---|
| Page | `frontend/src/pages/admin/AdminKnowledgeBasePage.tsx` (129 lines): table of articles, create/edit dialog (title, body), publish/unpublish, delete |
| Route / nav | `/admin/knowledge-base`; admin nav item `adminKnowledgeBase` (line 127), shown with `MANAGE_KNOWLEDGE_BASE` |
| API | `AdminKnowledgeArticleController` — `/admin/knowledge-base/articles` GET, GET `{id}`, POST, PUT `{id}`, POST `{id}/publish`, POST `{id}/unpublish`, DELETE `{id}` |
| Tests | `frontend/src/pages/admin/AdminKnowledgeBasePage.test.tsx`; backend `KnowledgeArticleServiceTest` |

## 3. Layout, header, sidebar and reusable components
- Shell: `components/layout/AppShell.tsx` (top bar with `TopBarSearch` — suggestions and Ctrl/Cmd+K since C70 — sidebar from `appNavigation.ts`), `PageHeader.tsx` (eyebrow/area, icon, title, subtitle, action), `SiteNavbar.tsx` (signed-out pages).
- UI components (`components/ui/`): `ConfirmDialog`, `EmptyState`, `ErrorState`, `StatusBadge`, `SummaryCard`, `ShowcaseCard`, `FeatureTag`, `FormSection`, `ColorPicker`, `SupportCTA`.
- Settings components (`components/settings/`): `SettingsSection` with `StatusTile`, `SaveBar`, `useSettingsForm`.
- Dashboard components (`components/dashboard/`, C69): `DashPanel`, `KpiCard`, `AnimatedBars`, `AnimatedDonut`, `MeterBar`, `useResource`, motion helpers that honour *Reduce motion*.
- Search components (`components/search/`, C70): `HighlightedText`, `MatchLabel`.
- **Not found:** a shared `DataTable`, `FilterBar` or drawer component (C44/C54 planned them; each page builds its own MUI `Table`). No rich-text or block editor, no drag-and-drop library, no video player.

## 4. Design tokens and theme
- `frontend/src/theme.ts` — `getTheme(mode, accent)`, MUI theme, Inter font.
- `frontend/src/theming/`: `accents.ts` (10 accent keys), `accentPalette.ts`, `ThemeModeProvider.tsx` (light/dark, accent), `LocalePreferenceProvider.tsx` (dates/times), `formatOptions.ts`.
- Unified colour system C45; design rules `docs/05-ui/screen-requirements/design-system.md` (C66).
- The visual direction canvas (claude.ai artifact G2rBj5KRcuyyDHeLTjfXas) uses Manrope / IBM Plex Sans and its own indigo/teal palette; the build follows the EIS theme (Inter, C45 accents) and takes the canvas's layout and sections only.

## 5. Authentication and authorization
- Keycloak JWT (`config/SecurityConfig.java`, `KeycloakJwtAuthenticationConverter`): Keycloak client roles become `ROLE_<NAME>` authorities.
- Platform permissions: `AuthorizationService#hasPlatformPermission` maps `ROLE_*` authorities to EIS `Role` rows (scope PLATFORM) and their `Permission`s; seeded by `RbacSeeder`. Endpoints use `permissions.platformPermission("…")` in `SecurityConfig`.
- **Knowledge permission today:** `MANAGE_KNOWLEDGE_BASE` (seeded for ADMIN): every `/admin/knowledge-base/**` call. `GET /knowledge-base/**` is `permitAll` (signed-out visitors can read published articles).
- Roles & permissions screens: `pages/admin/RolesAdminPage.tsx`, `PermissionsAdminPage.tsx` (REQ-IAM-003) — an admin can create platform roles and assign permissions to roles. **Not found:** a screen that gives a platform role to one person; platform roles reach a person only as a Keycloak client role (assigned in Keycloak). Privileged access (PAM, `PrivilegedAccessService`) grants temporary elevated permissions.
- Organization side: `OrgRole` (ORG_ADMIN, MEMBER), organization permissions, product access per member (`organization_product_access`), access-management redesign REQ-TEN-005 (Draft, not built). Entitlements REQ-SUB-002 (Draft, not built; decision C52 — the prompt's "C49").
- Search (C70) adds `MANAGE_SEARCH` for the search index, synonyms and insights.

## 6. Knowledge tables and migrations
- `knowledge_article` (id, title VARCHAR(200), body VARCHAR(20000) plain text, status DRAFT|PUBLISHED, version INT, created_at, updated_at) — migration V010 era; `backend/src/main/resources/db/schema.sql` line 689.
- No categories, products, modules, audiences, media, videos, versions history or feedback tables.
- Migrations: `database/migrations/V001–V020`; Flyway disabled (schema applied by hand).

## 7. API conventions
- Base path `/api` (context path), also `/v1/...` (REQ-INT-001). Public reads under the feature path (`/knowledge-base/...`), admin under `/admin/...`, own data under `/me/...`.
- Errors: `GlobalExceptionHandler` → `{ timestamp, status, error, message }` (+ `code` for some), 400 validation, 404 `ResourceNotFoundException`, 409 `InvalidStateException`, 500 generic message with server-side log only.
- Paging: no shared convention; pages that page return `{ items, totalElements, page, size }` (`AuditLogPageDto`, platform events). Most lists are unpaged.

## 8. File and media upload
- `modules/product/service/ProductImageService.java`: multipart upload through the backend to **local disk** (`app.file-upload-dir`), UUID file names, extension whitelist (jpg, jpeg, png, gif, webp); served by `GET /products/images/**`, `/platforms/images/**`. Its own comment notes S3 is needed for more than one instance.
- **Not found:** S3 or any cloud storage SDK, presigned URLs, video upload, document upload. D23 (product media storage) was open until this request.

## 9. Search
- REQ-PRT-002 (keyword) and REQ-PRT-003 (semantic, hybrid, pgvector) — built 2026-10-05 (C70). Index `search_document` (types PRODUCT, KNOWLEDGE, TICKET), `search_chunk` (pgvector, public only), `search_term`, `search_synonym`, `search_query_log`, `search_index_run`; `SearchChangeListener` re-indexes after commit; `SearchDocumentBuilder` maps records; admin `/admin/search` (`MANAGE_SEARCH`). Semantic search runs on the stub model until SS-1–SS-4 are answered.

## 10. Analytics and audit
- Audit: `modules/audit/service/AuditService` (`record`, `recordSuccess`, `recordFailure`) → `audit_log` (action, actor, target, organization, outcome, detail); 33 services already call it, including `KnowledgeArticleService` (KNOWLEDGE_ARTICLE_CREATED, …). Admin view `/admin/audit-logs`.
- Analytics: `product_usage` (launch count and last launch per user/product), `search_history_entry` (per user), `search_query_log` (C70 insights, no identity), platform/business dashboards. **Not found:** article views, video views, downloads or feedback tracking.

## 11. YouTube and video handling
- **None.** No video entity, no YouTube URL field, no player, no oEmbed or YouTube API call anywhere in backend or frontend. "Existing YouTube videos keep working" therefore has nothing to preserve today.

## Reuse plan
| Area | Decision | Why |
|---|---|---|
| Articles | **Extend** `knowledge_article` into the new content model (additive columns plus related tables); existing rows become content type ARTICLE, audience PUBLIC, status PUBLISHED/DRAFT unchanged; old endpoints and `/knowledge-base` route keep working (route redirects to the Knowledge Center) | No data or link loss (REQ-KNW-001) |
| Permissions | **Reuse** `MANAGE_KNOWLEDGE_BASE` as the publisher permission (proposed — confirm) and **add** one contributor permission; reuse `MANAGE_SEARCH` for full index rebuilds | Avoid a duplicate of the existing knowledge permission |
| Assigning people | **Reuse** platform roles (Roles & permissions screen) + Keycloak client-role assignment; how an admin gives the role to a person is an open question | No EIS screen for per-person platform roles exists |
| Authorization of readers | **Reuse** JWT, `CurrentCustomerResolver`, organization membership, `organization_product_access`, subscriptions, platform permissions | Rule: no new permission system |
| Storage | **New** `MediaStorageService` + `S3MediaStorageService` (AWS SDK v2, presigned URLs); `ProductImageService` unchanged (out of scope) | Nothing suitable exists; D23 → S3 |
| Video | **New** `VideoSourceProvider` (YouTube, AWS S3, external) | Nothing exists |
| Search | **Extend** the C70 index: new source types (videos, documents, FAQs, troubleshooting, glossary, release notes, templates, workflows, modules), transcripts and chapters as body text/passages, audience filter added to the visibility filter | Rule: no second search system; pgvector stays the only vector store |
| Audit | **Reuse** `AuditService` with new action names | Existing infrastructure |
| Analytics | **New** knowledge event table (views, downloads, plays, watch progress, feedback, ticket-from-knowledge); search signals **reuse** `search_query_log` | No article/video analytics exist |
| UI | **Reuse** shell, `PageHeader`, `StatusTile`, `KpiCard`, `DashPanel`, `ConfirmDialog`, `EmptyState`, `ErrorState`, `StatusBadge`, `HighlightedText`, `MatchLabel`, `TopBarSearch`; **new** block renderer, block editor, upload progress, video player, media picker; a shared table/filter component only if two or more knowledge screens need the same one | Nothing equivalent exists |
| Support | **Reuse** support tickets (12.01.01) for "Create support ticket" with prefilled fields | Existing feature |
| AI assistant | **New** interface only; endpoint answers "not configured" until D8 | Decision |
