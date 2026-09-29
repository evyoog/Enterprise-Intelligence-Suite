# Acceptance criteria — Platform Administration

Each criterion maps to at least one test case in `test-cases/functional/platform-administration/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** the backend has started **When** an admin lists currencies **Then** USD, EUR, GBP and INR all exist and are enabled | TC-GOV-001 |
| AC-2 | **Given** an enabled currency **When** an admin disables it, then re-enables it **Then** both changes are saved and returned | TC-GOV-002 |
| AC-3 | **Given** an admin **When** they create a region, then rename and disable it **Then** all three changes are saved | TC-GOV-003 |
| AC-4 | **Given** a region assigned to an organization **When** an admin tries to delete it **Then** the request is refused | TC-GOV-004 |
| AC-5 | **Given** an admin **When** they create, disable and delete a feature flag **Then** each change is saved, and the flag reads as enabled again once deleted (fails open) | TC-GOV-005 |
| AC-6 | **Given** no row exists for a flag key **When** any module checks it **Then** it reads as enabled | TC-GOV-006 |
| AC-7 | **Given** the "groups_enabled" flag is disabled **When** an organization admin tries any Groups action **Then** every one is refused, and re-enabling the flag restores them | TC-GOV-007 |
