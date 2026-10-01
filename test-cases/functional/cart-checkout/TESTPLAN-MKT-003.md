# TESTPLAN-MKT-003: Cart and checkout

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-003 |
| Requirement(s) covered | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) |
| Decision | [C59](../../../docs/01-business/roadmap/open-decisions.md#c59) |
| Author | Not specified |
| Status | Draft (the FRD is Draft; built 2026-10-01 at the product owner's request with the engineering defaults recorded under C59) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md): TC-MKT-011 to TC-MKT-026. The redesigned checkout screen is covered in [TESTPLAN-BIL-001](../billing-payments/TESTPLAN-BIL-001.md) (TC-BIL-019 to TC-BIL-023).

## Approach
Backend: `CartServiceTest` and `CartAuthorizationTest` against the H2-backed Spring context (Razorpay mocked). Frontend: Vitest with Testing Library and jest-axe, API mocked. Manual: cross-device persistence (TC-MKT-013) and the audit check (TC-MKT-024).

## Blocked or partial
- AC-6 tax line ("GST (18 %)"): needs the REQ-BIL-002 tax engine, not built yet.
