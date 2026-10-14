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
| AC-13 | REQ-SUB-001.5, .8, BR-SUB-011 | **Given** an ACTIVE subscription on plan Pro (500 a month) **when** the caller changes it to Basic (100 a month) **then** the plan is still Pro, `pendingPlanId`/`pendingPlanName` are Basic, `SUBSCRIPTION_PLAN_CHANGE_SCHEDULED` is audited and the owner is notified | [TC-SUB-038](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-038.md) |
| AC-14 | REQ-SUB-001.5, .10, BR-SUB-007 | **Given** a scheduled downgrade **when** the caller changes to a more expensive plan (Enterprise) **then** the plan becomes Enterprise at once and nothing is pending; **and** a plan of the same price as the current one is applied at once | [TC-SUB-039](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-039.md) |
| AC-15 | REQ-SUB-001.10, BR-SUB-013 | **Given** a scheduled downgrade **when** the caller withdraws it **then** nothing is pending, the plan is unchanged and `SUBSCRIPTION_PLAN_CHANGE_WITHDRAWN` is audited; withdrawing when nothing is pending changes nothing | [TC-SUB-040](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-040.md) |
| AC-16 | REQ-SUB-001.9, BR-SUB-012 | **Given** a scheduled downgrade and an ACTIVE subscription whose renewal date has passed **when** the auto-renew job runs **then** the plan is the pending plan and nothing is pending | [TC-SUB-041](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-041.md) |
| AC-17 | REQ-SUB-001.9, BR-SUB-012 | **Given** a scheduled downgrade **when** the customer renews manually **then** the plan becomes the pending plan, nothing is pending and the new term follows the new plan's billing period | [TC-SUB-042](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-042.md) |
| AC-18 | REQ-SUB-001.8, BR-SUB-011 | **Given** a monthly plan at 100 and a yearly plan at 1,000 (83.33 a month) **then** the yearly plan is a downgrade from the monthly plan and the monthly plan is not a downgrade from the yearly one | [TC-SUB-043](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-043.md) |
| AC-19 | REQ-SUB-001.8, .10 | **Given** My subscriptions with a scheduled downgrade **then** the subscription shows "Your plan changes to {plan} on {date}, at your next renewal. You keep your current plan until then." with **Keep current plan**, which withdraws it; the Change plan dialog says "An upgrade takes effect now. A downgrade takes effect at your next renewal." | [TC-SUB-044](../../../../test-cases/functional/subscription-lifecycle/TC-SUB-044.md) |
