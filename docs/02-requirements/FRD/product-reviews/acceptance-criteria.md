# Acceptance criteria — Product Reviews & Ratings

Each criterion maps to at least one test case in `test-cases/functional/product-reviews/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** a customer submits a review **Then** it is PENDING and is not counted in the product's public ratings until approved | TC-MKT-005 |
| AC-2 | **Given** a customer submits a second review for the same product **Then** it edits the existing row rather than creating a second, and resets it to PENDING even if it was previously approved | TC-MKT-006 |
| AC-3 | **Given** a PENDING review is rejected **Then** it never appears in the public ratings | TC-MKT-007 |
| AC-4 | **Given** a review has already been approved or rejected **When** an admin tries to decide it again **Then** the request is refused (400) | TC-MKT-008 |
| AC-5 | **Given** a product with a mix of approved and not-yet-approved reviews **Then** the average rating and count reflect only the approved ones | TC-MKT-009 |
| AC-6 | **Given** a nonexistent product **When** a review is submitted against it **Then** the request is refused (404) | TC-MKT-010 |
