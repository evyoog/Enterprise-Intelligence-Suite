# REQ-KNW-005 — Knowledge Center (user side)

**Status:** Draft — waits for "Approved"
**Owner:** Product owner
**Decisions:** [C71](../../../01-business/roadmap/open-decisions.md#c71)–[C77](../../../01-business/roadmap/open-decisions.md#c77)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (knowledge base); search parts [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) (01.03); Academy [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) (11b, P1 stretch) — dates unchanged |
| Requirement ID | REQ-KNW-005 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md); [01](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md) for search |
| Priority | P0 (Academy P1) |
| Visual direction | Canvas "EIS Knowledge Center redesign" (claude.ai/artifact/G2rBj5KRcuyyDHeLTjfXas), adapted to the EIS theme (C45): layout and sections from the canvas; fonts, colours and components from EIS |

## Summary
The existing `/knowledge-base` page becomes the **Knowledge Center**: a home with search, quick access, recommendations, popular guides, featured videos and a personal learning panel; product knowledge hubs; multimedia articles; workflow guides; troubleshooting with error codes; FAQs; downloads; templates; release notes; a Developer Center; a glossary; and (if confirmed) the Academy. Every list and search shows only content the reader may see (BR-KVS-001). The global header and sidebar stay.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-005.1 | **Route:** `/knowledge` (proposed — confirm) is the Knowledge Center; `/knowledge-base` redirects (REQ-KNW-001.9). Navigation label "Knowledge Center". | Must |
| REQ-KNW-005.2 | **Header:** title "Knowledge Center", subtitle "Learn, configure, troubleshoot and get the most from eVyoog.", search box "Search products, guides, videos, FAQs…" with suggestions, recent searches, Ctrl/Cmd+K and filters (reusing C70's search UI, scoped to knowledge types). | Must |
| REQ-KNW-005.3 | **Hero:** "Learn. Configure. Solve.", subtitle "Your complete guide to eVyoog products, features, workflows and support.", search box, popular searches (from search insights), a product UI visual, a video thumbnail with play button, subtle animation (off with reduced motion). | Must |
| REQ-KNW-005.4 | **Quick access:** six cards — Getting started, Product guides, Video tutorials, Troubleshooting, Downloads, FAQs — icon, title, description, hover animation, link to that section. | Must |
| REQ-KNW-005.5 | **Recommended for you:** products, articles, videos and workflows chosen from the reader's role, organization, product access, active subscriptions (entitlements REQ-SUB-002 when built; C52), recently viewed and used items and learning progress; with no signals, popular content. Signed-out readers see popular Public content. Ranking rules: **Not specified** beyond these signals (proposed: product access first, then recency, then popularity). | Must |
| REQ-KNW-005.6 | **Popular guides:** thumbnail, title, product, type, reading time, view count, updated date, action. | Must |
| REQ-KNW-005.7 | **Featured videos:** thumbnail, play button, duration, product, difficulty, views, title, source indicator (YouTube / Hosted / External). Video kinds: product tutorial, feature tutorial, getting started, troubleshooting, training, webinar, product demonstration (a video category list, data). "Featured" is set by publishers (proposed). | Must |
| REQ-KNW-005.8 | **Personal learning panel** (signed in): continue where you left off, recently viewed, recently searched (search history), bookmarks and saved items, learning progress. | Must |
| REQ-KNW-005.9 | **Support block** "Still need help?" on article, video, troubleshooting, FAQ and search-result pages: **Contact support**, **Create support ticket** (12.01.01) prefilled where permitted with current article, product, module, search query, error code and user role, **Ask AI assistant** (REQ-KNW-007, "Coming soon" until D8). Signed-out readers are asked to sign in for tickets. | Must |
| REQ-KNW-005.10 | **Search:** REQ-PRT-002/003 extended (C76) to the knowledge types (articles, guides, FAQs, troubleshooting and **error codes**, videos with **transcripts and chapters**, documents/PDFs with extracted text, image OCR text as an integration point, products, modules, workflows, templates, release notes, developer docs). Results grouped by type; meaning-based queries work through hybrid search; unauthorised content filtered before ranking (BR-KVS-001); results for products the reader has access to are boosted (Should). | Must |
| REQ-KNW-005.11 | **Products and hubs:** cards for Valam.ai, Varthan.ai, Yukth.ai, Thittam.ai, Thiran.ai, Tharav.ai (from taxonomy, C74) with logo/icon, screenshot, description, modules, content count, Explore guides, View product (catalog page). Each product has a hub listing its modules and their content. | Must |
| REQ-KNW-005.12 | **Article page:** renders blocks (REQ-KNW-002.2) in order; glossary terms are links; related articles, related product and a **direct action** link to an EIS page; reading time; last updated; version; breadcrumb (Knowledge Center › product › module). | Must |
| REQ-KNW-005.13 | **Feedback** on every article: "Was this helpful?" Yes/No; on No, a reason (information unclear, outdated, missing, couldn't solve my problem, other) and an optional comment; **Report outdated content** and **Suggest improvement** actions. Signed-out feedback: **Not specified** (proposed: Yes/No allowed, comments signed in only). | Must |
| REQ-KNW-005.14 | **Workflow guides:** interactive flows (Procure-to-pay, Lead-to-cash, Production — seed data); clicking a step opens its guide; defined as data by publishers (WORKFLOW_GUIDE). | Must |
| REQ-KNW-005.15 | **Troubleshooting:** categories (login, permissions, product access, configuration, transaction errors, integration, data, reports, performance) and error codes (for example `EIS-PO-001`) showing error, cause, solution, required permission, related documentation, video solution and support. | Must |
| REQ-KNW-005.16 | **FAQs:** accordions, filterable by product, module, feature, workflow and article. | Must |
| REQ-KNW-005.17 | **Downloads and resources:** user manuals, implementation/configuration guides, brochures, release notes, templates, checklists, sample documents, API documentation — file type, size, version, product, updated, View, Download (presigned, REQ-KNW-003.8). | Must |
| REQ-KNW-005.18 | **Template library** (seed: purchase order, RFQ, employee import, attendance import, chart of accounts, opening balance, BOM, routing). | Must |
| REQ-KNW-005.19 | **Release notes:** version, product, release date, new features, improvements, bug fixes, deprecated features, notices; filter by product. | Must |
| REQ-KNW-005.20 | **Developer Center** (structure): API documentation, authentication, API keys, REST APIs, webhooks, events, SDKs, code examples, request/response, errors, rate limits, integration guides; code blocks with Copy; links to REQ-INT-001 (API keys) and REQ-INT-002 (events). | Should |
| REQ-KNW-005.21 | **Glossary** (seed: BOM, MRP, OEE, WIP, S&OP, MES, QMS, CAPA, RFQ, MOQ, EOQ); terms linkable from articles. | Must |
| REQ-KNW-005.22 | **Academy** (11b, P1 stretch): courses → lessons → quizzes/assessments → progress → completion → certificates. Built only if confirmed; otherwise the data model and navigation are prepared and the section is hidden. | Could |
| REQ-KNW-005.23 | **Video player:** REQ-KNW-004.6. | Must |
| REQ-KNW-005.24 | **Analytics** events are sent for views, plays, progress, downloads, searches, feedback and tickets created (REQ-KNW-006). | Must |
| REQ-KNW-005.25 | **Design, motion, accessibility, responsive, i18n:** EIS theme (C45); subtle motion with reduced-motion support; keyboard, focus, labels, contrast, accessible video controls, captions; axe tests on every screen and dialog; tablet collapses secondary controls, mobile shows cards; all strings under `knowledge.*` in `en.json` and `es.json`. | Must |

## Out of scope
- Answer generation (REQ-KNW-007, D8).
- Other EIS screens (no redesign).

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | May signed-out visitors read Public content? (Today they can read published articles.) | Yes |
| 2 | Academy / certificates now, or in its planned sprint 2027.1.3 (stretch)? | Yes (for .22) |
| 3 | Route `/knowledge` (proposed) or keep `/knowledge-base`? | No — confirm in review |
| 4 | Recommendation ranking beyond the listed signals. | No — confirm in review |
| 5 | Error-code format across products (for example `EIS-PO-001`) and who owns the list. | No — confirm in review |
| 6 | Signed-out feedback. | No — confirm in review |
| 7 | "Difficulty" values for videos (proposed Beginner, Intermediate, Advanced). | No — confirm in review |
