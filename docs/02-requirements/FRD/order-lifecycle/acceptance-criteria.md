# Acceptance criteria — Order Lifecycle

Each criterion maps to at least one test case in `test-cases/functional/order-lifecycle/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an active member of an organization **When** they submit an order for an ACTIVE product with one of its own plans **Then** the order is created with status SUBMITTED | TC-ORD-001 |
| AC-2 | **Given** a member submitting an order **When** the chosen plan belongs to a different product **Then** the request is refused (400) | TC-ORD-002 |
| AC-3 | **Given** a SUBMITTED order **When** an ORG_ADMIN (MANAGE_ORDERS) of the same organization approves it **Then** the order becomes APPROVED and the organization's subscription to that product/plan is created (or reactivated) as ACTIVE | TC-ORD-003 |
| AC-4 | **Given** a SUBMITTED order **When** a plain MEMBER (no MANAGE_ORDERS) attempts to approve it **Then** the request is refused (403) | TC-ORD-004 |
| AC-5 | **Given** a SUBMITTED order **When** an ORG_ADMIN rejects it with a note **Then** the order becomes REJECTED and the note is recorded | TC-ORD-005 |
| AC-6 | **Given** a REJECTED or APPROVED order **When** the requester attempts to cancel it **Then** the request is refused (400) | TC-ORD-006 |
| AC-7 | **Given** a SUBMITTED order **When** its own requester cancels it **Then** the order becomes CANCELLED | TC-ORD-007 |
| AC-8 | **Given** an order belonging to a different organization **When** any action is attempted on it **Then** the response is a generic 404, never confirming the order exists | TC-ORD-008 |
