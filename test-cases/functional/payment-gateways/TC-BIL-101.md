# TC-BIL-101: Billing settings embeds the same gateway list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-101 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.1) |
| Acceptance Criterion | [AC-31](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-2](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 1 (The page shows the registered providers) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A backend with the summary `{registered: 5, implemented: 1, configured: 1, active: 1}` and one gateway row.

## Steps
1. Open `/admin/billing/settings`.
2. Read the overview tile.
3. Open the Payment gateways tab.

## Expected Result
The tab is named "Payment gateways"; the tile shows "1 of 1 active"; the tab shows the gateway list from the same component.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "shows the payment gateways tab with the same list as the Payment Gateways page (C98)"
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "shows the four overview tiles and the business tab, with no axe violations"

## Actual Result
The automated tests listed above passed (frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
