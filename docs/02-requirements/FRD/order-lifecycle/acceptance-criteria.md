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
| AC-9 | **Given** a member who cannot buy directly **when** their cart of two products becomes a purchase request with a reason **then** one order with both products and the reason is SUBMITTED (REQ-ORD-001.8, .9), every administrator is notified with the products and the reason, no invoice exists, and `GET …/orders` returns `items` (two) and `requestReason`; **when** a blank reason is sent from the cart **then** it is refused and nothing is created | TC-ORD-009 |
| AC-10 | **Given** a SUBMITTED order of two products **when** an ORG_ADMIN approves it **then** both products are provisioned, **one** invoice with two lines exists and the order is APPROVED (REQ-ORD-001.10) | TC-ORD-010 |
| AC-11 | **Given** an organization administrator, or a member with Manage billing, **when** they buy from the cart **then** an order is recorded APPROVED by the buyer ("Purchased directly"), no request is raised, one invoice is created, and the administrators are notified only when the buyer is not an administrator (REQ-ORD-001.11); **given** the Orders page **when** it lists a multi-product request or a legacy single-product order **then** every product and the reason are shown | TC-ORD-011 |

