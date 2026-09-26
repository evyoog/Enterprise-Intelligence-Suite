# Acceptance criteria — Member Lifecycle & Access Review

Each criterion maps to at least one test case in `test-cases/functional/member-lifecycle/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an active member **When** an org admin suspends them **Then** their status is SUSPENDED and they can no longer access any self-service endpoint | TC-TEN-011 |
| AC-2 | **Given** a suspended member and a free seat **When** an org admin reactivates them **Then** their status is ACTIVE | TC-TEN-012 |
| AC-3 | **Given** a suspended member and no free seat **When** an org admin reactivates them **Then** the request is refused with a seat-limit error | TC-TEN-013 |
| AC-4 | **Given** an organization with only one active admin **When** that admin tries to suspend or remove themself **Then** the request is refused | TC-TEN-014 |
| AC-5 | **Given** a removed member **When** an org admin tries to reactivate them **Then** the request is refused | TC-TEN-015 |
| AC-6 | **Given** a member never reviewed **When** an org admin reviews their access **Then** the member shows a review timestamp and reviewer | TC-TEN-016 |
