# UI requirements — Subscription Lifecycle

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| My subscriptions | `/my/subscriptions` | Any authenticated customer | Not specified |

Linked from the existing "Your dashboard" page (`/my/products`) via a "Manage subscriptions" button — that page's own data (`DashboardProduct`) carries only a subscription's status, not its id, so it cannot itself call these actions.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Plan (change-plan dialog) | Select | No (empty = clear) | Must be a plan of the subscription's own product | Backend message shown as-is (BR-SUB-007) |

## States
- Empty: "You have no subscriptions yet." (`subscriptions.noSubscriptions`)
- Loading: a centered spinner while the initial list loads
- Error: the backend's own message shown in an `Alert`, dismissible, without discarding the rest of the list
- Per-row actions are disabled (not hidden) while that row's own action is in flight

## Accessibility and localization
- Every action is a labeled `<button>` (MUI `Button`), reachable and operable by keyboard; the change-plan dialog uses a labeled `Select` (`InputLabel` + `labelId`).
- Cancel asks for a native `window.confirm` before calling the backend (BR-SUB-003 is one-way).
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `subscriptions.*` — no hard-coded user-facing text.
