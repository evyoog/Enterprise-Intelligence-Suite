# Acceptance criteria — Product Recommendations

Each criterion maps to at least one test case in `test-cases/functional/product-recommendations/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an ACTIVE product marked featured **When** recommendations are requested **Then** it appears in the featured list | TC-MKT-001 |
| AC-2 | **Given** an ACTIVE product NOT marked featured **When** recommendations are requested **Then** it does not appear in the featured list | TC-MKT-002 |
| AC-3 | **Given** two products with different total launch counts across different customers **When** recommendations are requested **Then** the one with more total launches ranks ahead of the one with fewer | TC-MKT-003 |
| AC-4 | **Given** a product with launches but currently INACTIVE **When** recommendations are requested **Then** it does not appear in the popular list | TC-MKT-004 |
