# TESTPLAN-BIL-002: Tax Rules and Calculation

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) |
| Acceptance criteria | [acceptance-criteria.md](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [business-rules.md](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Screen | [admin-tax-rules.md](../../../docs/05-ui/screen-requirements/admin-tax-rules.md) |
| Decisions | [C96](../../../docs/01-business/roadmap/open-decisions.md#c96), [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |

Covers the External Tax Engine switch (internal rules engine or external provider), the internal engine (rules, approval and versions, India GST and other countries, rounding, exemptions, preview), the external provider adapter (validation before enabling, retry, no fall-back, response validation, credentials), Draft invoices and Retry tax, tax lines on invoices and credit notes, GSTIN format, invoice numbering, authorization and audit.

## How the cases run
- Backend cases run in `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/**`, `config/TaxAuthorizationTest.java` and the billing service tests, against an H2-backed Spring context. Every rate in these tests is a rule the test approves itself: the platform ships no rate. The external provider is a stub (`StubTaxProvider`) or a local HTTP server; no real provider is ever called.
- Frontend cases run in Vitest with Testing Library and jest-axe, with the API mocked: `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx`, `BillingPage.test.tsx`, `CheckoutPage.test.tsx`.
- Run with `scripts/build.sh`, or `cd backend && mvn -B verify` and `cd frontend && npm test`.
- Steps that no automated test covers (sign-out and restart persistence, audit log entries, log and database inspection, keyboard and screen-reader use, real concurrency) are listed under "Not automated" in the case.
- Status of every case is Not Run until a tester or a CI run is recorded in the case.

## Cases
| Case | Title | Acceptance criteria | Priority | Automated |
|---|---|---|---|---|
| [TC-BIL-029](TC-BIL-029.md) | The External Tax Engine switch shows the active mode and its status, and the mode persists | AC-1 | P0 | Partly |
| [TC-BIL-030](TC-BIL-030.md) | External mode can be switched on only after the provider passes every check, and the change is audited | AC-2 | P0 | Partly |
| [TC-BIL-031](TC-BIL-031.md) | Switching the engine off is always allowed and warns when no rule is in force | AC-3 | P0 | Yes |
| [TC-BIL-032](TC-BIL-032.md) | Editing provider settings while external mode is on must pass validation, and settings never carry credentials | AC-2, AC-22 | P0 | Yes |
| [TC-BIL-033](TC-BIL-033.md) | A rule goes from Draft to Approved through a different administrator (maker and checker) | AC-4 | P0 | Yes |
| [TC-BIL-034](TC-BIL-034.md) | A new version of an approved rule closes the previous version the day before | AC-4 | P0 | Yes |
| [TC-BIL-035](TC-BIL-035.md) | Rules are validated when saved: components, rates, dates, states and classes | AC-4 | P1 | Yes |
| [TC-BIL-036](TC-BIL-036.md) | Only Approved, active rules that are in force on the invoice date are used | AC-5 | P0 | Yes |
| [TC-BIL-037](TC-BIL-037.md) | India GST: CGST + SGST within the seller's state, IGST between states, place of supply from a valid GSTIN | AC-6 | P0 | Yes |
| [TC-BIL-038](TC-BIL-038.md) | Other countries use the same rule structure with their own components and conditions | AC-7 | P1 | Yes |
| [TC-BIL-039](TC-BIL-039.md) | Several lines and components are rounded half up per line and per component; compound components | AC-7 | P0 | Yes |
| [TC-BIL-040](TC-BIL-040.md) | The most specific rule wins and equally specific rules block the invoice as ambiguous | AC-8 | P0 | Yes |
| [TC-BIL-041](TC-BIL-041.md) | An approved exemption makes the result an explicit Exempt with the reason | AC-9 | P0 | Yes |
| [TC-BIL-042](TC-BIL-042.md) | Tax classes, product classification and supplier registrations | — | P1 | Yes |
| [TC-BIL-043](TC-BIL-043.md) | With no matching rule the invoice stays a Draft with a reason; no tax is assumed | AC-10 | P0 | Yes |
| [TC-BIL-044](TC-BIL-044.md) | The preview calculator shows components or the block reason and creates nothing | — | P2 | Yes |
| [TC-BIL-045](TC-BIL-045.md) | Test connection and Validate capabilities show the result and no secret | AC-11 | P0 | Yes |
| [TC-BIL-046](TC-BIL-046.md) | External mode: the validated breakdown is stored and printed; an unsupported country is blocked | AC-12 | P0 | Yes |
| [TC-BIL-047](TC-BIL-047.md) | Transient provider failures are retried with the same idempotency key; permanent failures are not | AC-13 | P0 | Yes |
| [TC-BIL-048](TC-BIL-048.md) | There is no silent fall-back: a failing provider blocks the invoice and internal rules are not used | AC-13 | P0 | Yes |
| [TC-BIL-049](TC-BIL-049.md) | An invalid provider response is rejected and the invoice is blocked | AC-14 | P0 | Yes |
| [TC-BIL-050](TC-BIL-050.md) | Provider credentials never appear in any API response, log message or screen | AC-22 | P0 | Yes |
| [TC-BIL-051](TC-BIL-051.md) | Switching the engine affects only new calculations; issued invoices and credit notes never change | AC-15 | P0 | Yes |
| [TC-BIL-052](TC-BIL-052.md) | A calculation in progress keeps the engine it started with (pinned configuration) | AC-16 | P0 | Yes |
| [TC-BIL-053](TC-BIL-053.md) | A Draft invoice is hidden from the customer, cannot be paid, and Retry tax restarts the calculation | AC-17 | P0 | Yes |
| [TC-BIL-054](TC-BIL-054.md) | The issued invoice shows subtotal, each tax component, total, place of supply, tax class and SAC/HSN code | AC-18 | P0 | Yes |
| [TC-BIL-055](TC-BIL-055.md) | GSTIN format is validated for Indian customers; other countries accept free text | AC-19 | P0 | Yes |
| [TC-BIL-056](TC-BIL-056.md) | A refund issues a credit note that reverses the original tax lines in proportion | AC-20 | P0 | Yes |
| [TC-BIL-057](TC-BIL-057.md) | Invoice numbers follow the format EIS/26-27/000123 and restart each financial year | AC-17 | P1 | Yes |
| [TC-BIL-058](TC-BIL-058.md) | Only MANAGE_BILLING can use tax endpoints; every change is audited | AC-21 | P0 | Partly |
| [TC-BIL-059](TC-BIL-059.md) | The Tax configuration screen: states, keyboard and screen-reader access | AC-1 | P1 | Partly |

## Acceptance criteria coverage
| Acceptance criterion | Test cases |
|---|---|
| AC-1 | TC-BIL-029, TC-BIL-059 |
| AC-2 | TC-BIL-030, TC-BIL-032 |
| AC-3 | TC-BIL-031 |
| AC-4 | TC-BIL-033, TC-BIL-034, TC-BIL-035 |
| AC-5 | TC-BIL-036 |
| AC-6 | TC-BIL-037 |
| AC-7 | TC-BIL-038, TC-BIL-039 |
| AC-8 | TC-BIL-040 |
| AC-9 | TC-BIL-041 |
| AC-10 | TC-BIL-043 |
| AC-11 | TC-BIL-045 |
| AC-12 | TC-BIL-046 |
| AC-13 | TC-BIL-047, TC-BIL-048 |
| AC-14 | TC-BIL-049 |
| AC-15 | TC-BIL-051 |
| AC-16 | TC-BIL-052 |
| AC-17 | TC-BIL-053, TC-BIL-057 |
| AC-18 | TC-BIL-054 |
| AC-19 | TC-BIL-055 |
| AC-20 | TC-BIL-056 |
| AC-21 | TC-BIL-058 |
| AC-22 | TC-BIL-032, TC-BIL-050 |

Requirements without an acceptance criterion of their own: REQ-BIL-002.19 and .37 (TC-BIL-042), .21 (TC-BIL-044).
