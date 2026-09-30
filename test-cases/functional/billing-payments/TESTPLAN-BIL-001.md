# TESTPLAN-BIL-001: Billing & Payments

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance criteria | [acceptance-criteria.md](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C46](../../../docs/01-business/roadmap/open-decisions.md#c46), [C47](../../../docs/01-business/roadmap/open-decisions.md#c47) |

Covers invoice generation, paying an invoice through Razorpay (mocked — no real Razorpay call is ever made in these tests), refunds, webhook idempotency and signature checks, saved payment methods, and gateway status. Every case below runs against a real H2-backed Spring context (`@SpringBootTest`) with only `RazorpayClient` mocked — entities, repositories and service logic are real.
