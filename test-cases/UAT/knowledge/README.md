# UAT — Knowledge Center and Knowledge Management (REQ-KNW-001 to REQ-KNW-008)

Step-by-step scenarios for a business user. Requirements: [knowledge FRDs](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) (REQ-KNW-002 to 008, and the REQ-KNW-001 update); test plan [TESTPLAN-KNW-002](../../functional/knowledge-base/TESTPLAN-KNW-002.md). Built on 2026-10-05; runnable now. Scripts 03 needs a test S3 bucket.

| Script | Scenario | Acceptance criteria |
|---|---|---|
| [UAT-KNW-001](UAT-KNW-001-permissions-and-publishing.md) | Give a person a knowledge role; draft, review, publish, version | REQ-KNW-008 AC-1–5, REQ-KNW-002 AC-1–5, AC-7 |
| [UAT-KNW-002](UAT-KNW-002-media-library.md) | Upload, use, replace and delete files | REQ-KNW-003 AC-1–4, AC-6 |
| [UAT-KNW-003](UAT-KNW-003-videos-and-s3.md) | YouTube, hosted and external videos; secure playback | REQ-KNW-004 AC-1–8, REQ-KNW-003 AC-5 |
| [UAT-KNW-004](UAT-KNW-004-reader-experience.md) | Knowledge Center as a reader: home, search, article, feedback, support | REQ-KNW-005 AC-1–10, REQ-KNW-006, REQ-KNW-007 |

**Test data:** seed data (6 products and their modules, categories, glossary, workflows, template drafts). Users: Priya (platform ADMIN), Karthik (Keycloak client role `KNOWLEDGE_WRITER` holding `KNOWLEDGE_CONTRIBUTE` only), Asha (member of "UAT Org" with Valam.ai access), Ravi (member of another organization), and a signed-out browser.

**S3 test bucket (script 03):** private, Block Public Access on, bucket owner enforced, versioning on, CORS allowing PUT and GET from the EIS web origin and exposing `ETag`. Set `EIS_KNOWLEDGE_STORAGE_PROVIDER=s3`, `EIS_KNOWLEDGE_BUCKET`, `EIS_KNOWLEDGE_REGION`; keys only in `config/secrets.env` (or an IAM role). See [aws-s3.md](../../../docs/09-integrations/aws-s3.md).

Record for each run: tester, date, environment, result (Pass / Fail), defect link.
