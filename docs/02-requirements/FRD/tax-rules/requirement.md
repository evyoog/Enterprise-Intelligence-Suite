# REQ-BIL-002 — Tax Rules and Calculation

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Cannot be approved until the open questions below marked **Blocks approval** are answered.
**Decision:** [C51](../../../01-business/roadmap/open-decisions.md#c51)

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-BIL-002 |
| Application | [08 Billing & Payments](../../../01-business/roadmap/applications/08-billing-payments.md) |
| Application code | `APP-BIL` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 08.05.01 | Configure tax rules | Yes |
| 08.05.01 | Calculate tax | Yes |
| 08.05.01 | Validate tax | Partly — see Open questions |

## Summary
Every invoice REQ-BIL-001 generates needs a tax amount. This feature lets a platform administrator configure, per region, how tax is calculated — an admin-entered rate, or (once a tax service is chosen) an external tax service — and calculates each invoice's tax at issue time using the customer's region. The rate used is copied onto the invoice line so later rule changes never change a finalized invoice ([C51](../../../01-business/roadmap/open-decisions.md#c51)).

## Actors
- Platform administrator (`MANAGE_BILLING`) — configure tax rules, preview a calculation
- `InvoiceService` (system) — calls the tax calculation at invoice-generation time

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-BIL-002.1 | A platform administrator (`MANAGE_BILLING`) can create, edit, enable and disable a tax rule per region: region (from existing platform regions, REQ-GOV-001.2), tax name (for example "GST"), rate (percentage, 0–100, up to 2 decimals), tax method (Admin rate / Tax service), effective-from date, status. | Must |
| REQ-BIL-002.2 | At most one enabled rule exists per region per effective date. | Must |
| REQ-BIL-002.3 | When an invoice is generated, tax is calculated for the customer's region using that region's method; the Tax service method falls back to the region's admin rate if the service is not configured or fails, and the invoice records the method actually used (Admin rate or Tax service). | Must |
| REQ-BIL-002.4 | If a region has no enabled rule, tax is 0 and the invoice shows "No tax rule for this region". | Should — confirm in review |
| REQ-BIL-002.5 | The tax rate used is copied onto the invoice; later rule changes never change already-finalized invoices. | Must |
| REQ-BIL-002.6 | Tax-service credentials live only in the common secrets file ([BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md)); until a service is chosen, the Tax service method shows **Not configured** and always falls back to the admin rate. | Must |
| REQ-BIL-002.7 | Every tax-rule change (create, edit, enable, disable) is written to the audit log. | Must |

## Out of scope
- Which external tax service is integrated — Not specified (Open question).
- Tax-inclusive vs tax-exclusive plan pricing — Not specified (Open question, blocks approval).
- India CGST/SGST vs IGST splitting — Not specified (Open question, blocks approval for India).
- Validating a customer's tax ID (for example GSTIN format) or whether a valid tax ID changes the tax applied — Not specified (Open questions).
- Price books, promotions, usage billing — later, per [C50](../../../01-business/roadmap/open-decisions.md#c50).
- Multi-jurisdiction tax (a single customer taxed by more than one region at once) — Not specified.

## Dependencies
- Existing platform regions ([REQ-GOV-001.2](../platform-administration/requirement.md), `platform_region`).
- [REQ-BIL-001](../billing-payments/requirement.md) — the invoice this feature's calculation is copied onto.
- New table described in [07-database/data-model/billing-payments.md](../../../07-database/data-model/billing-payments.md) (`tax_rule`, plus `invoice`/`invoice_line` additions).
- New permission reuse: `MANAGE_BILLING` (platform ADMIN) — same permission as REQ-BIL-001, no new one.
- Common secrets file: [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), feature business rules, feature workflow, acceptance criteria | this folder |
| Screens (UI) | [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) — see [ui-requirements.md](ui-requirements.md) |
| API | [docs/06-api/api-requirements/tax-rules.md](../../../06-api/api-requirements/tax-rules.md) — see [api-requirements.md](api-requirements.md) |
| Cross-feature rules | [BR-SEC-001](../../../03-business-rules/BR-SEC-001-central-secrets-file.md) |
| Data model | [docs/07-database/data-model/billing-payments.md](../../../07-database/data-model/billing-payments.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Tax service provider:** which external tax service is used for the Tax service method? | No — the Admin rate method and its fallback work without this; confirm in review |
| 2 | **Tax-inclusive vs tax-exclusive pricing:** are plan prices tax-inclusive or tax-exclusive? | Yes |
| 3 | **India GST split:** should invoices split CGST + SGST (same state) versus IGST (different state), based on the seller's and the customer's state? | Yes — blocks approval for India |
| 4 | **Validate tax (08.05.01.03):** what is validated — the customer's tax ID format (for example GSTIN), or the calculated amount? | No — confirm in review |
| 5 | **Tax ID effect:** does a valid customer tax ID change the tax applied (for example business-to-business / reverse-charge rules)? | No — confirm in review |
