# UI requirements — Subscription Lifecycle

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| My subscriptions | `/my/subscriptions` | Any authenticated customer | Not specified. Screen requirement: [my-subscriptions.md](../../../05-ui/screen-requirements/my-subscriptions.md) |

Linked from the existing "Your dashboard" page (`/my/products`) via a "Manage subscriptions" button — that page's own data (`DashboardProduct`) carries only a subscription's status, not its id, so it cannot itself call these actions.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Plan (change-plan dialog) | Select | No (empty = clear) | Must be a plan of the subscription's own product | Backend message shown as-is (BR-SUB-007) |

The change-plan dialog shows the rule above the select: "An upgrade takes effect now. A downgrade takes effect at your next renewal." (`subscriptions.changePlanDialog.rule`, [C97](../../../01-business/roadmap/open-decisions.md#c97) SC-1). A subscription with a scheduled downgrade shows an info notice — "Your plan changes to {plan} on {date}, at your next renewal. You keep your current plan until then." (`subscriptions.scheduledChange.notice`; "{date}" is the term end, or "your next renewal" when there is none) — with a **Keep current plan** button (`subscriptions.scheduledChange.keep`) that withdraws it (BR-SUB-013).

## States
- Empty: "You have no subscriptions yet." (`subscriptions.noSubscriptions`)
- Loading: a centered spinner while the initial list loads
- Error: the backend's own message shown in an `Alert`, dismissible, without discarding the rest of the list
- Per-row actions (including Keep current plan) are disabled (not hidden) while that row's own action is in flight

## Accessibility and localization
- Every action is a labeled `<button>` (MUI `Button`), reachable and operable by keyboard; the change-plan dialog uses a labeled `Select` (`InputLabel` + `labelId`).
- Cancel asks for a native `window.confirm` before calling the backend (BR-SUB-003 is one-way).
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `subscriptions.*` — no hard-coded user-facing text.
