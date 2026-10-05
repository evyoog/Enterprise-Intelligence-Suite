# REQ-KNW-002 — Knowledge content model

**Status:** Draft — waits for "Approved"
**Owner:** Product owner
**Decisions:** [C71](../../../01-business/roadmap/open-decisions.md#c71), [C74](../../../01-business/roadmap/open-decisions.md#c74), [C77](../../../01-business/roadmap/open-decisions.md#c77)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (planned; dates unchanged) |
| Requirement ID | REQ-KNW-002 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md) |
| Priority | P0 |
| Source functions | 11.01.01 Create, Edit, Publish, Search, Version article (extended to every content type) |

## Summary
One content model for every kind of knowledge: typed items with metadata, taxonomy (product, module, feature, category), audience, an ordered list of blocks, a publishing workflow with versions, and a preview that renders exactly what readers will see. Existing articles become items of type ARTICLE (REQ-KNW-001.7).

## Content types (proposed — confirm: the prompt says "15 content types" but does not list them)
| Type | Used for |
|---|---|
| ARTICLE | General articles (existing articles migrate here) |
| GETTING_STARTED | Getting-started guides |
| PRODUCT_GUIDE | Step-by-step product and module guides |
| VIDEO | Videos (REQ-KNW-004) |
| DOCUMENT | Manuals, implementation/configuration guides, brochures, checklists, sample documents (file via REQ-KNW-003) |
| TEMPLATE | Downloadable templates (purchase order, RFQ, imports, …) |
| STUDY_MATERIAL | Learning material used by courses |
| RELEASE_NOTE | Release notes per product and version |
| FAQ | Question and answer |
| TROUBLESHOOTING | Problem, cause, solution |
| ERROR_CODE | One error code with error, cause, solution, required permission |
| GLOSSARY_TERM | Term and definition, linkable from text |
| WORKFLOW_GUIDE | Interactive process (steps linked to guides) |
| DEVELOPER_DOC | Developer Center pages |
| COURSE | Academy course (prepared; built only if confirmed — C77) |

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-002.1 | Every content item has: type, title, short description, slug, product, module, feature, category, tags, keywords, audience (BR-KVS-001), product version, documentation version, effective date, review date, expiry date, author, reviewer, approver, created/updated/published dates and version. | Must |
| REQ-KNW-002.2 | The body is an ordered list of **blocks**: heading, paragraph, bulleted list, numbered list, quote, image, gallery, video, audio, file, PDF, table, code, callout, warning, note, step, accordion, FAQ, button, link, workflow diagram, embedded content. Media blocks reference media library items (REQ-KNW-003) or videos (REQ-KNW-004), never raw storage URLs. | Must |
| REQ-KNW-002.3 | Type-specific fields: FAQ (question, answer); TROUBLESHOOTING (category, problem, cause, solution, error codes); ERROR_CODE (code, error, cause, solution, required permission, related documentation, video solution); GLOSSARY_TERM (term, definition, synonyms); RELEASE_NOTE (version, release date, new features, improvements, bug fixes, deprecated features, notices); WORKFLOW_GUIDE (ordered steps, each linking to content and optionally an EIS page); DOCUMENT/TEMPLATE (file, file type, size, version); DEVELOPER_DOC (blocks with code samples). | Must |
| REQ-KNW-002.4 | **Taxonomy is data** (C74): knowledge products (linked to catalog products), modules per product and categories are tables managed by publishers; the prompt's module lists are seed data (see data model). | Must |
| REQ-KNW-002.5 | **Workflow:** Draft → In review → Approved → Scheduled → Published → Deprecated → Archived (see workflow). Contributors save drafts and submit; publishers approve or return to draft, schedule, publish, unpublish, deprecate, archive. No existing approval rule is bypassed. | Must |
| REQ-KNW-002.6 | **Versions:** every publish records a version (1.0, 1.1, 2.0 — the publisher chooses minor or major, proposed — confirm). A publisher can view any previous version, compare two versions (block by block, text differences), restore a version (it becomes a new draft) and deprecate. | Must |
| REQ-KNW-002.7 | **Preview:** renders the current draft with the Knowledge Center's own components, for the chosen audience, before publishing. | Must |
| REQ-KNW-002.8 | **Relations:** related content, related product, glossary terms used, and a **direct action** link to an EIS page. | Should |
| REQ-KNW-002.9 | Items past their review date are listed as "Requires review"; items past their expiry date stop being shown to readers (Expired). | Must |
| REQ-KNW-002.10 | Every create, edit, state change, version restore and delete is audited (BR-KPRM-005). | Must |
| REQ-KNW-002.11 | Published changes re-index the item in search (C76, REQ-PRT-002 `SearchChangeListener`); unpublished, expired, archived items leave the index. | Must |

## Out of scope
- Organization-authored content (C71 open question).
- Real-time co-editing; comments on drafts (Not specified).

## Dependencies
- REQ-KNW-001 (migration), REQ-KNW-003/004 (media), REQ-KNW-008 (permissions), REQ-PRT-002/003 (search), `AuditService`.
- Data model: [`knowledge.md`](../../../07-database/data-model/knowledge.md). API: [`knowledge.md`](../../../06-api/api-requirements/knowledge.md).

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | The list of 15 content types above (proposed). | Yes |
| 2 | Version numbering: publisher chooses minor or major on publish (proposed)? | No — confirm in review |
| 3 | Is a separate Approval step needed, or does a publisher's approval equal publishing? (Prompt lists Review and Approval as separate states.) | No — confirm in review |
| 4 | Who may set the review and expiry dates, and what happens at expiry beyond hiding (notify the author?) — Not specified. | No — confirm in review |
| 5 | Feature taxonomy (below module): a free-text field or a managed list? Not specified. | No — confirm in review |
