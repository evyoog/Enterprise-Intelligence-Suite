# Acceptance criteria — Product Lifecycle & Structure

Each criterion maps to at least one test case in `test-cases/functional/product-lifecycle/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a product at version 1 **When** an admin updates it twice **Then** its version is 3 | TC-CAT-001 |
| AC-2 | **Given** an INACTIVE product **When** an admin publishes it **Then** its status is ACTIVE and it appears on the public storefront | TC-CAT-002 |
| AC-3 | **Given** an ACTIVE product with an existing subscription **When** an admin retires it **Then** its status is RETIRED, it no longer appears in `GET /products`, and the existing subscription is unchanged | TC-CAT-003 |
| AC-4 | **Given** a RETIRED product **When** an admin publishes it **Then** its status is ACTIVE again | TC-CAT-004 |
| AC-5 | **Given** two products **When** an admin sets one as the other's parent with a variant label **Then** both fields are saved and returned | TC-CAT-005 |
| AC-6 | **Given** a product that is another's parent **When** an admin tries to delete it **Then** the request is refused with a 409 | TC-CAT-006 |
| AC-7 | **Given** a product **When** an admin tries to set it as its own dependency **Then** the request is refused with a 400 | TC-CAT-007 |
