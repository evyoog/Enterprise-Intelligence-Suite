# Acceptance criteria — Knowledge permissions

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a signed-in customer without knowledge permissions **When** they call any knowledge write endpoint (create, upload-url, complete-upload, update, publish, delete, replace) **Then** each answers 403 and nothing changes | [TC-KNW-019](../../../../test-cases/functional/knowledge-permissions/TC-KNW-019.md) |
| AC-2 | **Given** a contributor **When** they create a draft, upload a file and submit for review **Then** it succeeds; **When** they try to publish or delete published content **Then** 403 | [TC-KNW-020](../../../../test-cases/functional/knowledge-permissions/TC-KNW-020.md) |
| AC-3 | **Given** a publisher **When** they approve, publish, unpublish, archive, restore and delete **Then** each succeeds and is audited | [TC-KNW-021](../../../../test-cases/functional/knowledge-permissions/TC-KNW-021.md) |
| AC-4 | **Given** a user without a knowledge permission **When** they open the app **Then** no Knowledge Management navigation or edit entry point is shown | [TC-KNW-022](../../../../test-cases/functional/knowledge-permissions/TC-KNW-022.md) |
| AC-5 | **Given** a role change granting or removing a knowledge permission **When** it is saved **Then** the audit log records it | [TC-KNW-023](../../../../test-cases/functional/knowledge-permissions/TC-KNW-023.md) |
