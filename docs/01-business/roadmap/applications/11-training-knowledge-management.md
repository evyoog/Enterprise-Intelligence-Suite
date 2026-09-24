# 11 Training & Knowledge Management

| Field | Value |
|---|---|
| Application ID | 11 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Training & Knowledge Management |
| Description | Documentation, courses, labs and certifications ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| Capabilities / features / functions | 4 / 7 / 26 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [11.01](#1101-knowledge-base) | Knowledge Base | 11.01.01 Knowledge Articles, 11.01.02 AI Knowledge | P0 | Yes | No |
| [11.02](#1102-learning-management) | Learning Management | 11.02.01 Courses, 11.02.02 Learning Paths | P0 | Yes | No |
| [11.03](#1103-training-delivery) | Training Delivery | 11.03.01 Labs & Assessments, 11.03.02 Video Learning | P0 | Yes | No |
| [11.04](#1104-certification) | Certification | 11.04.01 Certificates | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 11.01 Knowledge Base

### Feature 11.01.01 Knowledge Articles

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.01.01.01 | Create article | No | No | Platform Service | `/knowledge-base/create-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.02 | Edit article | No | No | Platform Service | `/knowledge-base/edit-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.03 | Publish article | No | No | Platform Service | `/knowledge-base/publish-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.04 | Search article | No | No | Platform Service | `/knowledge-base/search-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.05 | Version article | No | No | Platform Service | `/knowledge-base/version-article` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

### Feature 11.01.02 AI Knowledge

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.01.02.01 | Index content | No | No | Platform Service | `/knowledge-base/index-content` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.02.02 | Retrieve relevant content | No | No | Platform Service | `/knowledge-base/retrieve-relevant-content` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.02.03 | Validate source | No | No | Platform Service | `/knowledge-base/validate-source` | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

## 11.02 Learning Management

### Feature 11.02.01 Courses

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.02.01.01 | Create course | No | No | Platform Service | `/learning-management/create-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.02 | Publish course | No | No | Platform Service | `/learning-management/publish-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.03 | Enroll user | No | No | Platform Service | `/learning-management/enroll-user` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.04 | Track progress | No | No | Platform Service | `/learning-management/track-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.05 | Complete course | No | No | Platform Service | `/learning-management/complete-course` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

### Feature 11.02.02 Learning Paths

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.02.02.01 | Create learning path | No | No | Platform Service | `/learning-management/create-learning-path` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.02.02 | Assign learning path | No | No | Platform Service | `/learning-management/assign-learning-path` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.02.03 | Track path progress | No | No | Platform Service | `/learning-management/track-path-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

## 11.03 Training Delivery

### Feature 11.03.01 Labs & Assessments

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.03.01.01 | Launch lab | No | Yes | AI Agent | `/training-delivery/launch-lab` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.02 | Submit assessment | No | Yes | AI Agent | `/training-delivery/submit-assessment` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.03 | Score assessment | No | Yes | AI Agent | `/training-delivery/score-assessment` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.04 | Track completion | No | Yes | AI Agent | `/training-delivery/track-completion` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |

### Feature 11.03.02 Video Learning

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.03.02.01 | Stream video | No | Yes | AI Agent | `/training-delivery/stream-video` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.02.02 | Track watch progress | No | Yes | AI Agent | `/training-delivery/track-watch-progress` | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |

## 11.04 Certification

### Feature 11.04.01 Certificates

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.04.01.01 | Define certification | No | No | Platform Service | `/certification/define-certification` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.02 | Issue certificate | No | No | Platform Service | `/certification/issue-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.03 | Verify certificate | No | No | Platform Service | `/certification/verify-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.04 | Expire certificate | No | No | Platform Service | `/certification/expire-certificate` | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02 | DE-019 KnowledgeArticle and DE-020 Course relate to Product | [WB:Data Entities] |
| 04, 12 | UJ-008 and UJ-009 use Knowledge | [WB:User Journeys] |

## Deliverables

Not specified in any source. [WB:Traceability] links these functions to the API and microservice columns in the tables above; those are the nearest implied deliverables.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | Not created |
| Requirement | `docs/02-requirements/functional-requirements/REQ-<APP-CODE>-<NNN>.md` | Not created. No REQ-IDs exist in the sources |
| Business rules | `docs/03-business-rules/` | Not specified in the sources |
| Test cases | `test-cases/functional/<feature>/TC-<APP-CODE>-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
