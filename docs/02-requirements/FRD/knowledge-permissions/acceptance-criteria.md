# Acceptance criteria — Knowledge permissions

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a signed-in customer without knowledge permissions **When** they call any knowledge write endpoint (create, upload-url, complete-upload, update, publish, delete, replace) **Then** each answers 403 and nothing changes | To be written with the build |
| AC-2 | **Given** a contributor **When** they create a draft, upload a file and submit for review **Then** it succeeds; **When** they try to publish or delete published content **Then** 403 | To be written with the build |
| AC-3 | **Given** a publisher **When** they approve, publish, unpublish, archive, restore and delete **Then** each succeeds and is audited | To be written with the build |
| AC-4 | **Given** a user without a knowledge permission **When** they open the app **Then** no Knowledge Management navigation or edit entry point is shown | To be written with the build |
| AC-5 | **Given** a role change granting or removing a knowledge permission **When** it is saved **Then** the audit log records it | To be written with the build |
