# 11 Training & Knowledge Management

| Field | Value |
|---|---|
| Application ID | 11 ([WB] numbering) |
| Application code | `APP-KNW` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `KNW`, for example `REQ-KNW-001` |
| Application | Training & Knowledge Management |
| Description | Documentation, courses, labs and certifications ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | Split ([C31](../open-decisions.md#c31)): 11a Knowledge base in [2027.1.1](../sprints/SPRINT-2027.1.1.md) (1–31 Jan 2027); 11b Learning & certification in [2027.1.3](../sprints/SPRINT-2027.1.3.md) (1–31 Mar 2027) |
| Capabilities / features / functions | 4 / 7 / 26 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.

## Capabilities

MVP, priority and phase follow [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5) and [C6](../open-decisions.md#c6). The [WB] MVP, priority and phase columns are ignored. Where a capability is not placed in any [WB:Roadmap] workstream, its phase and priority are Not specified.

| Capability ID | Capability | Features | MVP | Priority | Phase | Basis |
|---|---|---|---|---|---|---|
| [11.01](#1101-knowledge-base) | Knowledge Base | 11.01.01 Knowledge Articles, 11.01.02 AI Knowledge | Yes | Phase 1 / MVP | P0 | C4 |
| [11.02](#1102-learning-management) | Learning Management | 11.02.01 Courses, 11.02.02 Learning Paths | No | Phase 2 | P1 | [WB:Roadmap] "Learning" |
| [11.03](#1103-training-delivery) | Training Delivery | 11.03.01 Labs & Assessments, 11.03.02 Video Learning | No | Phase 2 | P1 | [WB:Roadmap] "Learning" |
| [11.04](#1104-certification) | Certification | 11.04.01 Certificates | No | Phase 2 | P1 | [WB:Roadmap] "Learning" |

## 11.01 Knowledge Base

### Feature 11.01.01 Knowledge Articles

[C71](../open-decisions.md#c71)–[C77](../open-decisions.md#c77) (2026-10-05): extended into the Knowledge Center and Knowledge Management CMS — REQ-KNW-001 update and REQ-KNW-002–006, 008 — approved and built early on 2026-10-05 ([C78](../open-decisions.md#c78)). Planned sprint 2027.1.1 unchanged.

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.01.01.01 | Create article | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/create-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.01.02 | Edit article | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/edit-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.01.03 | Publish article | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/publish-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.01.04 | Search article | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/search-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.01.05 | Version article | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/version-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |

### Feature 11.01.02 AI Knowledge

[C75](../open-decisions.md#c75)/[C76](../open-decisions.md#c76) (2026-10-05): retrieval reuses platform search (REQ-PRT-002/003); the AI assistant is specified in [knowledge-assistant](../../../02-requirements/FRD/knowledge-assistant/requirement.md) and built as "not configured" until D8 (2026-10-05).

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.01.02.01 | Index content | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/index-content` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.02.02 | Retrieve relevant content | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/retrieve-relevant-content` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.01.02.03 | Validate source | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/knowledge-base/validate-source` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |

## 11.02 Learning Management

### Feature 11.02.01 Courses

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.02.01.01 | Create course | No | Phase 2 | P1 | No | Platform Service | `/learning-management/create-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.01.02 | Publish course | No | Phase 2 | P1 | No | Platform Service | `/learning-management/publish-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.01.03 | Enroll user | No | Phase 2 | P1 | No | Platform Service | `/learning-management/enroll-user` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.01.04 | Track progress | No | Phase 2 | P1 | No | Platform Service | `/learning-management/track-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.01.05 | Complete course | No | Phase 2 | P1 | No | Platform Service | `/learning-management/complete-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |

### Feature 11.02.02 Learning Paths

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.02.02.01 | Create learning path | No | Phase 2 | P1 | No | Platform Service | `/learning-management/create-learning-path` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.02.02 | Assign learning path | No | Phase 2 | P1 | No | Platform Service | `/learning-management/assign-learning-path` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.02.02.03 | Track path progress | No | Phase 2 | P1 | No | Platform Service | `/learning-management/track-path-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |

## 11.03 Training Delivery

### Feature 11.03.01 Labs & Assessments

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.03.01.01 | Launch lab | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/launch-lab` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |
| 11.03.01.02 | Submit assessment | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/submit-assessment` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |
| 11.03.01.03 | Score assessment | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/score-assessment` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |
| 11.03.01.04 | Track completion | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/track-completion` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |

### Feature 11.03.02 Video Learning

[C73](../open-decisions.md#c73)/[C77](../open-decisions.md#c77) (2026-10-05): video library and watch progress built early on 2026-10-05 ([knowledge-videos](../../../02-requirements/FRD/knowledge-videos/requirement.md), C78). Planned sprint 2027.1.3 unchanged.

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.03.02.01 | Stream video | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/stream-video` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |
| 11.03.02.02 | Track watch progress | No | Phase 2 | P1 | Yes | AI Agent | `/training-delivery/track-watch-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection |

## 11.04 Certification

### Feature 11.04.01 Certificates

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.04.01.01 | Define certification | No | Phase 2 | P1 | No | Platform Service | `/certification/define-certification` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.04.01.02 | Issue certificate | No | Phase 2 | P1 | No | Platform Service | `/certification/issue-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.04.01.03 | Verify certificate | No | Phase 2 | P1 | No | Platform Service | `/certification/verify-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |
| 11.04.01.04 | Expire certificate | No | Phase 2 | P1 | No | Platform Service | `/certification/expire-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02 | DE-019 KnowledgeArticle and DE-020 Course relate to Product | [WB:Data Entities] |
| 04, 12 | UJ-008 and UJ-009 use Knowledge | [WB:User Journeys] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-KNW-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Knowledge Center FRDs (2026-10-05, Draft) | [knowledge-content](../../../02-requirements/FRD/knowledge-content/requirement.md), [knowledge-media](../../../02-requirements/FRD/knowledge-media/requirement.md), [knowledge-videos](../../../02-requirements/FRD/knowledge-videos/requirement.md), [knowledge-center](../../../02-requirements/FRD/knowledge-center/requirement.md), [knowledge-analytics](../../../02-requirements/FRD/knowledge-analytics/requirement.md), [knowledge-assistant](../../../02-requirements/FRD/knowledge-assistant/requirement.md), [knowledge-permissions](../../../02-requirements/FRD/knowledge-permissions/requirement.md) (REQ-KNW-002–008) and REQ-KNW-001 update; inventory [knowledge-center-inventory.md](../../../08-architecture/knowledge-center-inventory.md) | Approved and built early on 2026-10-05 ([C78](../open-decisions.md#c78)) |
| Test cases | `test-cases/functional/knowledge-base/TC-KNW-001..006.md`; TC-KNW-007–054 in `test-cases/functional/knowledge-*/`; UAT `test-cases/UAT/knowledge/` | Created (REQ-KNW-001 to 008) |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/knowledgebase (KnowledgeArticle, KnowledgeArticleService, KnowledgeArticleController, AdminKnowledgeArticleController — 11.01.01 extended 2026-10-05: content model, workflow, versions, taxonomy, media (S3), videos, analytics, search source, assistant stub)`
- Frontend: `frontend/src/pages/knowledge/` (Knowledge Center, `/knowledge`), `frontend/src/pages/knowledge-admin/` (Knowledge Management, `/knowledge-management`), `frontend/src/components/knowledge/`, `frontend/src/api/knowledgeApi.ts` (the old pages were replaced; their routes redirect)
