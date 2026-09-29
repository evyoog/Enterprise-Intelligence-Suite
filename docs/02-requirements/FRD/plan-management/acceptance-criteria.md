# Acceptance criteria — Plan Management

Each criterion maps to at least one test case in `test-cases/functional/plan-management/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a new plan with no currency sent **When** it is created **Then** its currency is USD | TC-CAT-008 |
| AC-2 | **Given** a plan with usage limit, included features, usage price, overage charge and tier-pricing text **When** it is created **Then** all five fields are saved and returned unchanged | TC-CAT-009 |
| AC-3 | **Given** an existing plan with no pricing fields **When** its product is updated without touching those fields **Then** the plan still behaves exactly as before this feature | TC-CAT-010 |
