# Acceptance criteria — Entitlements (REQ-SUB-002)

Source for test cases in `test-cases/functional/entitlements/` (created when the feature is built).

| ID | Requirement | Given / When / Then |
|---|---|---|
| AC-1 | REQ-SUB-002.1, BR-2 | **Given** an ACTIVE subscription to a plan with `includedFeatures` "A, B" and `usageLimit` 100 **when** entitlements are checked **then** the response lists features A and B and limit 100, read live from the plan — no entitlement row exists anywhere. |
| AC-2 | REQ-SUB-002.2 | **Given** a customer with two ACTIVE subscriptions **when** they view their entitlements **then** each shows product, plan, status, term end, included features and usage limits. |
| AC-3 | REQ-SUB-002.3, BR-4 | **Given** an organization with subscriptions to two products **when** an allowed organization user views entitlements **then** both subscriptions' entitlements are shown as one list. |
| AC-4 | REQ-SUB-002.6, BR-1 | **Given** a subscription that is SUSPENDED, CANCELLED or EXPIRED **when** its entitlement is checked **then** the result is not-allowed for every feature of that product. |
| AC-5 | REQ-SUB-002.4 | **Given** a feature key not in the plan's `includedFeatures` **when** checked **then** the result is not-allowed, even though the subscription itself is ACTIVE. |
| AC-6 | REQ-SUB-002.5, BR-5 | **Given** a call to `POST /internal/entitlements/check` without a valid `X-Internal-Sso-Secret` **then** it is refused with 403. |
| AC-7 | BR-4 | **Given** customer A **when** they request customer B's entitlements through the customer-facing endpoint **then** the response is 404. |
