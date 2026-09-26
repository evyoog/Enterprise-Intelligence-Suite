# Acceptance criteria — Group Management

Each criterion maps to at least one test case in `test-cases/functional/group-management/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an org admin **When** they create a group named "Engineering" **Then** it appears in their organization's group list with no members | TC-TEN-017 |
| AC-2 | **Given** an empty group and a member of the same organization **When** the admin adds that member **Then** the group lists them as a member | TC-TEN-018 |
| AC-3 | **Given** a member already in a group **When** the admin adds them again **Then** the group still lists them exactly once | TC-TEN-019 |
| AC-4 | **Given** a group with a member **When** the admin removes that member **Then** the group has no members | TC-TEN-020 |
| AC-5 | **Given** a group in another organization **When** an admin tries to add a member to it **Then** the request is refused with a 404 | TC-TEN-021 |
| AC-6 | **Given** a group **When** the admin deletes it **Then** it no longer appears in the organization's group list | TC-TEN-022 |
