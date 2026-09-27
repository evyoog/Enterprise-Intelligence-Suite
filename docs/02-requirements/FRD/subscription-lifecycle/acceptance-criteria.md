# Acceptance criteria — Subscription Lifecycle

Each criterion maps to at least one test case in `test-cases/functional/subscription-lifecycle/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** an ACTIVE subscription owned by the caller **When** they suspend it **Then** its status becomes SUSPENDED | TC-SUB-001 |
| AC-2 | **Given** a SUSPENDED subscription **When** the caller suspends it again **Then** the request is refused (400) | TC-SUB-002 |
| AC-3 | **Given** a SUSPENDED subscription owned by the caller **When** they reactivate it **Then** its status becomes ACTIVE | TC-SUB-003 |
| AC-4 | **Given** any subscription owned by the caller **When** they cancel it **Then** its status becomes CANCELLED, and no further suspend/reactivate/renew/change-plan action on it succeeds | TC-SUB-004 |
| AC-5 | **Given** an ACTIVE subscription on a MONTHLY plan **When** the caller renews it **Then** `expiresAt` extends by 30 days from the later of now or the current `expiresAt` | TC-SUB-005 |
| AC-6 | **Given** an EXPIRED subscription **When** the caller renews it **Then** its status becomes ACTIVE and `expiresAt` extends | TC-SUB-006 |
| AC-7 | **Given** a subscription with no plan and no prior `expiresAt` **When** the caller renews it **Then** the request is refused (400) | TC-SUB-007 |
| AC-8 | **Given** an ACTIVE subscription **When** the caller changes its plan to one belonging to the same product **Then** `planId`/`planName` update accordingly | TC-SUB-008 |
| AC-9 | **Given** an ACTIVE subscription **When** the caller changes its plan to one belonging to a different product **Then** the request is refused (400) | TC-SUB-009 |
| AC-10 | **Given** a subscription owned by a different customer **When** the caller attempts any action on it **Then** the response is a generic 404, never confirming the subscription exists | TC-SUB-010 |
| AC-11 | **Given** an ACTIVE subscription whose `expiresAt` has passed **When** the scheduled expiry job runs **Then** its status becomes EXPIRED | TC-SUB-011 |
| AC-12 | **Given** an ACTIVE subscription whose `expiresAt` is in the future **When** the scheduled expiry job runs **Then** its status is unchanged | TC-SUB-012 |
