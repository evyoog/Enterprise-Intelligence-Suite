# Acceptance criteria — Tenant Lifecycle

Each criterion maps to at least one test case in `test-cases/functional/tenant-lifecycle/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an organization and an existing region **When** an admin assigns the region and turns on seat overage **Then** both are saved and returned, including the region's name | TC-TEN-023 |
| AC-2 | **Given** an organization **When** an admin assigns a region id that does not exist **Then** the request is refused | TC-TEN-024 |
| AC-3 | **Given** an organization at its seat limit with seat overage allowed **When** an admin adds another member **Then** the member is added, and the organization is reported as over its limit | TC-TEN-025 |
| AC-4 | **Given** an organization at its seat limit with seat overage **not** allowed **When** an admin adds another member **Then** the request is refused | TC-TEN-026 |
