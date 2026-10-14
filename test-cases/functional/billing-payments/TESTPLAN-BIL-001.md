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

## Billing settings (C60, REQ-BIL-001.24)
TC-BIL-024 to TC-BIL-028: business profile and invoicing (prefix, payment terms, issuer on documents), full offline details and accepted methods, payment-method switches and Razorpay Checkout appearance, the Razorpay credentials view (BR-SEC-001), accessibility.

## Sprint 2026.4.3 answers ([C97](../../../docs/01-business/roadmap/open-decisions.md#c97), REQ-BIL-001.25 to .33)
TC-BIL-060 to TC-BIL-070 (acceptance criteria AC-36 to AC-46): purchase starts after payment (BI-1: TC-BIL-060 to 062), invoice number, terms and reminders (BI-2: TC-BIL-063 to 065), pay by invoice for enabled organizations (BI-4: TC-BIL-066), the Manage billing permission (BI-5: TC-BIL-067), policy links (BI-7: TC-BIL-068), the payment gateways list (BI-8: TC-BIL-069) and the purchase-order number (BI-9: TC-BIL-070). BI-9's refunds by administrators only, equal-amount bank payments and no fees line (AC-47) are covered by the existing [TC-BIL-005](TC-BIL-005.md) and [TC-BIL-016](TC-BIL-016.md). Invoice tax, credit notes and the tax engine are in [tax-rules](../tax-rules/).

| Test Case ID | Title | Priority | Automated? |
|---|---|---|---|
| [TC-BIL-060](TC-BIL-060.md) | Online purchase pending until paid, starts once | P0 | Yes |
| [TC-BIL-061](TC-BIL-061.md) | Pay by invoice starts an enabled organization's purchase | P0 | Yes |
| [TC-BIL-062](TC-BIL-062.md) | Tax-blocked Draft cannot be paid | P1 | Yes (partly) |
| [TC-BIL-063](TC-BIL-063.md) | Reminder kind by days left | P0 | Yes |
| [TC-BIL-064](TC-BIL-064.md) | Each reminder once, stops when paid | P0 | Yes |
| [TC-BIL-065](TC-BIL-065.md) | Invoice number format and 15-day terms | P1 | Yes |
| [TC-BIL-066](TC-BIL-066.md) | Pay by invoice only for enabled organizations | P0 | Yes |
| [TC-BIL-067](TC-BIL-067.md) | Manage billing permission, reads included | P0 | Yes |
| [TC-BIL-068](TC-BIL-068.md) | Policy links: settings, checkout, invoice | P1 | Yes |
| [TC-BIL-069](TC-BIL-069.md) | Payment gateways list and active gateway | P1 | Yes |
| [TC-BIL-070](TC-BIL-070.md) | Purchase-order number | P1 | Yes |
