# Knowledge Center — home

**Status:** Draft (2026-10-05) — not built. Requirement: REQ-KNW-005.1–.9.

| Field | Value |
|---|---|
| Route | `/knowledge` (proposed; `/knowledge-base` redirects) |
| Roles | Readers (Public signed out if confirmed) |

## Content
| Area | Content |
|---|---|
| Page header | Title "Knowledge Center", subtitle "Learn, configure, troubleshoot and get the most from eVyoog.", search box "Search products, guides, videos, FAQs…" (suggestions, recent searches, Ctrl/Cmd+K, filters by type) |
| Hero (dark navy) | "Learn. Configure. Solve.", "Your complete guide to eVyoog products, features, workflows and support.", search box, popular searches chips (from search insights), product UI visual, video thumbnail with play button, subtle animation |
| Quick access | Six cards: Getting started, Product guides, Video tutorials, Troubleshooting, Downloads, FAQs — icon, title, one-line description, hover lift, link to the section |
| Products | Six product cards (taxonomy): logo/icon, screenshot, description, modules, content count, Explore guides, View product |
| Recommended for you | Mixed cards (product, article, video, workflow) from role, organization, product access, subscriptions, recent activity, progress; fallback popular |
| Popular guides | Rows: thumbnail, title, product, type, reading time, views, updated, Open |
| Featured videos | Cards: thumbnail, play button, duration, product, difficulty, views, title, source badge (YouTube / Hosted / External) |
| Personal learning panel (signed in) | Continue where you left off (progress bar, Resume), Recently viewed, Recently searched, Bookmarks, Learning progress |
| Latest updates | Release notes (newest 3) and most searched this week |
| Still need help? | Contact support, Create support ticket, Ask AI assistant ("Coming soon") |

## Common rules
- EIS shell (global header and sidebar) unchanged; EIS theme (C45, Inter, accent palette); layout and section order follow the visual direction canvas (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas).
- Content shown only if the reader may see it ([BR-KVS-001](../../03-business-rules/BR-KVS-001-knowledge-visibility.md)).
- States: skeleton loading, empty (EmptyState with a next step), error (ErrorState with Retry).
- Motion: subtle hover, transitions and skeletons; none with *Reduce motion* or `prefers-reduced-motion`.
- Accessibility: keyboard, visible focus, labels, 4.5:1 contrast, semantic controls; axe test.
- Responsive: desktop primary; tablet collapses secondary controls; mobile stacks cards.
- i18n: `knowledge.*` in `en.json` and `es.json`.
