# UI requirements — Member Lifecycle & Access Review

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Members and roles (Organization settings; moved from the business dashboard by [C69](../../../01-business/roadmap/open-decisions.md#c69)) | `/organization/settings#members` | Org admin, platform admin | Not specified |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Status chip | Chip (Active/Suspended/Removed) | n/a | n/a | n/a |
| Suspend / Reactivate / Remove | Buttons, shown by current status | n/a | Backend enforces last-admin and seat-limit rules | `orgSettings.statusSaveError` |
| Review | Button | n/a | n/a | `orgSettings.reviewSaveError` |
| Last reviewed | Caption text | n/a | n/a | n/a |

## States
- Empty: "Never reviewed" caption when `lastReviewedAt` is absent.
- Loading: per-row `savingId` disables that row's controls while a request is in flight (existing pattern from role-change).
- Error: inline `Alert` above the table, same as the existing role-change/MFA-reset errors.

## Accessibility and localization
- Every action button has an explicit `aria-label` including the member's name (e.g. "Suspend Bob Byte").
- All new labels go through `t()` — `orgSettings.suspend`, `.reactivate`, `.remove`, `.review`, `.statuses.*`, `.reviewedOn`, `.neverReviewed` — in both `en.json` and `es.json`.
- Remove asks for confirmation via `window.confirm` (existing convention in this codebase, e.g. `ProductGrid.tsx`).
