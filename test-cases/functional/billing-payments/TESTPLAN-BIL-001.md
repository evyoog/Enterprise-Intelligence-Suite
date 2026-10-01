# TESTPLAN-BIL-001: Billing & Payments

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance criteria | [acceptance-criteria.md](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C46](../../../docs/01-business/roadmap/open-decisions.md#c46), [C47](../../../docs/01-business/roadmap/open-decisions.md#c47), [C55](../../../docs/01-business/roadmap/open-decisions.md#c55) |

Covers invoice generation, paying an invoice through Razorpay (mocked — no real Razorpay call is ever made in these tests), refunds, webhook idempotency and signature checks, saved payment methods, and gateway status. Every case below runs against a real H2-backed Spring context (`@SpringBootTest`) with only `RazorpayClient` mocked — entities, repositories and service logic are real.

## Checkout and offline payment (C55, REQ-BIL-001.18–.21)
TC-BIL-009 to TC-BIL-018 cover the three-step checkout (payment options, consent gating, card panel without card data, Razorpay card/UPI/other, failure and cancel, gateway not configured), Pay by invoice, the admin Record offline payment dialog, the offline bank details settings and accessibility (axe). Frontend cases run in Vitest with Testing Library and jest-axe, with the API mocked; backend cases run in `CheckoutOfflinePaymentTest` and `OfflineBillingAuthorizationTest` against the H2-backed Spring context.

## Redesigned checkout (C59, REQ-BIL-001.18, .22, .23)
TC-BIL-019 to TC-BIL-023 cover the redesigned Payment step (breadcrumb, billing summary, Amount due, method tiles, cart summary), Netbanking and Wallets preselection, saved cards, the Complete order button and the payment brand assets. Cart-side cases are in [TESTPLAN-MKT-003](../cart-checkout/TESTPLAN-MKT-003.md).
