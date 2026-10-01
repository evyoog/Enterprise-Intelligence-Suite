# Shared UI standards — Billing screens

Applies to every screen listed in [billing-payments ui-requirements.md](../../02-requirements/FRD/billing-payments/ui-requirements.md), [tax-rules ui-requirements.md](../../02-requirements/FRD/tax-rules/ui-requirements.md) and [entitlements ui-requirements.md](../../02-requirements/FRD/entitlements/ui-requirements.md). These are presentation requirements only — they add no business rules; where a rule is needed, the feature's own FRD is the source.

## Brand and theme
Use the unified MUI theme from [C45](../../01-business/roadmap/open-decisions.md#c45) (`frontend/src/theme.ts`). Every billing screen supports light and dark mode with no hard-coded colours — all colour comes from theme tokens.

## Shared components
Billing screens use `<DataTable>` (consistent header, sort, pagination, empty state) and `<FilterBar>` (consistent search, status chips, date range), planned in [C44](../../01-business/roadmap/open-decisions.md#c44) (`docs/08-architecture/ui-ux-redesign.md`). If these components do not exist yet when a billing screen is built, they are created at that point and used **only** on billing screens — retrofitting other admin lists onto them is a separate, already-carried task ([C44](../../01-business/roadmap/open-decisions.md#c44)'s own phased order) and is not done here.

## Status chips
One consistent mapping across every billing screen. Always icon + text — colour is never the only signal (accessibility).

| State | Chip colour |
|---|---|
| OPEN | info |
| PAID / CAPTURED / ACTIVE / Configured | success |
| PARTIALLY_REFUNDED / Expired / Pending | warning |
| FAILED / Not configured | error |
| REFUNDED / VOID / REMOVED / Disabled | neutral |

## Money
Formatted with `Intl.NumberFormat` using the platform's currency settings ([REQ-BIL-001.15](../../02-requirements/FRD/billing-payments/requirement.md)). Amounts are right-aligned with tabular figures in tables; totals are visually emphasised (bold / larger type).

## Dates
Shown in the platform date format. Relative time (for example "2 days ago") appears only in a tooltip on hover/focus, never as the only rendering of a date.

## Responsive layout
Tables collapse into stacked cards below 600px. Drawers (invoice detail, payment detail, tax-rule edit) become full-screen on mobile. Primary actions (Pay, Save, Refund, Add payment method) stay reachable without horizontal scrolling at any width.

## Loading / empty / error states
- **Loading:** skeleton placeholders matching the final layout (rows, cards), not a single generic spinner, for every list and summary card.
- **Empty:** a one-line explanation plus the next action (for example "No invoices yet." with nothing further to do; "No saved payment methods." with an **Add payment method** button).
- **Error:** the backend's own message shown as-is, with a **Retry** button.

## Feedback
Every action (save, pay, refund, enable/disable a tax rule, add/remove a payment method) shows a success or failure toast. A destructive action (remove payment method, refund, disable a tax rule) requires a confirmation dialog that names the specific item being acted on.

## Accessibility
WCAG 2.1 AA. Tabs, menus and dialogs are fully keyboard-reachable with a visible focus indicator. Payment-status changes (Pay invoice's Processing → Success/Failed/Pending) use `aria-live` so screen-reader users hear the update without re-focusing. Every billing screen and dialog has an automated accessibility (axe) test ([billing-payments AC-18](../../02-requirements/FRD/billing-payments/acceptance-criteria.md)).

## i18n
Every user-facing string exists in `frontend/src/i18n/locales/en.json` and `es.json`. No hard-coded English strings in billing components.

## Gateway banner
One shared component, used on every screen where Razorpay-dependent actions could appear (Billing, Add payment method, Pay invoice, Billing admin): "Online payments are not available yet. You can still view invoices and update billing details." ([REQ-BIL-001.14](../../02-requirements/FRD/billing-payments/requirement.md)).
