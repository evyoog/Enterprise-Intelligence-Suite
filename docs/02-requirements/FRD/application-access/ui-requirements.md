# UI requirements — Application access requests (REQ-TEN-010)

## Screens
| Screen | Route | Component | Roles | Notes |
|--------|-------|-----------|-------|-------|
| Access management → Access requests tab | `/organization/access?tab=requests` | `AccessRequestsTab` (`frontend/src/components/access/AccessRequestsTab.tsx`), inside `AccessManagementPage` (`frontend/src/pages/AccessManagementPage.tsx`) | Organization admins and *Manage product access* holders ([REQ-TEN-005](../access-management/requirement.md)) | A new fourth tab alongside Members, Role defaults and Activity |
| My requests | `/my/access-requests` | `MyAccessRequestsPage` (`frontend/src/pages/MyAccessRequestsPage.tsx`) | Any authenticated member | The requester's own request history |
| My products — request / remove access | `/my/products` | `MyProductsPage` (`frontend/src/pages/MyProductsPage.tsx`), request and remove dialogs inline on each product card | Any authenticated organization member | Entry point for raising a request or removing one's own access |

## Access requests tab (admin side)
A table listing every access request for the organization (`GET /organization/me/access-requests/all`), newest first:

| Column | Source |
|---|---|
| Member | `memberName` |
| Product | `productName` |
| Status | `StatusBadge` — PENDING (warning), APPROVED (success), REJECTED (error), CANCELLED (neutral) |
| Message | `message`, or an em dash when none |
| Requested | `createdAt`, localized |
| Decision | `decidedByName` + `decidedAt`, and `decisionNote` when rejected; an em dash when not yet decided |
| Actions | **Approve** and **Reject**, shown only while `status === 'PENDING'` |

- **Approve** calls `POST /organization/me/access-requests/{id}/approve` directly (no confirmation dialog) and replaces the row in place; the button disables (`busyId`) while in flight.
- **Reject** opens a dialog (`Dialog` with `aria-labelledby="reject-access-request-title"`) requiring a non-blank reason (multiline `TextField`); submitting with a blank reason shows an inline error (`accessRequests.noteRequired`) without a round trip. On success the row updates in place.
- An action's failure (403/404/409/400 from the backend) shows a dismissable `Alert` above the table with the backend's message where available, otherwise `accessRequests.actionError`.
- Empty state: `EmptyState` with `accessRequests.none` / `accessRequests.noneDescription`. Loading and error states follow the shared `ErrorState` pattern with a retry.

## My requests page (member side)
`/my/access-requests` lists the caller's own requests (`GET /organization/me/access-requests`) as cards, newest first, each showing the product name, the member's own message, the submitted date, a status `Chip`, and — only while PENDING — a **Cancel** button (`POST /organization/me/access-requests/{id}/cancel`). A REJECTED card also shows the decision note. Empty state: `accessRequests.myRequestsNone` / `accessRequests.myRequestsNoneDescription`.

## My products — request and remove access
On each product card in `MyProductsPage` (`ProductCard`, lines ~400–463), when the viewer is an organization member (`isOrgMember`) and the organization's subscription to that product is ACTIVE (`canManageOwnAccess`):
- If the member does **not** have access (`myAccessAssigned === false`): a **Request access** button opens a dialog to enter an optional message and calls `accessRequestApi.request`; while a request is already PENDING for that product (`hasPendingRequest`, computed from `GET /organization/me/access-requests`) the button is disabled and reads **Request pending**.
- If the member **does** have access (`myAccessAssigned === true`): a **Remove access** button (destructive styling) opens a confirmation dialog and calls `accessRequestApi.removeMyAccess`; on success it shows a success toast (`accessRequests.removeSuccess`) and reloads the dashboard.
- A product the member does not belong to as an organization member, or whose organization subscription is not ACTIVE, shows neither button (matches BR-AAC-001).

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|---------------------------|
| Request message | Multiline text | No | Trimmed; blank stored as none; backend caps at 500 characters | `accessRequests.requestError` (on backend refusal) |
| Reject reason | Multiline text | Yes | Non-blank before submit; backend also refuses blank | `accessRequests.noteRequired` (client), backend message (server) |

## States
- Empty: `accessRequests.none` (admin tab), `accessRequests.myRequestsNone` (my requests page).
- Loading: a `Typography` placeholder (`accessRequests.loading`) while the list fetch is in flight.
- Error: `ErrorState` with a retry action calling the same fetch.
- Busy: the acted-on row's buttons disable via `busyId`/`removeBusy`/`requestBusy` while a call is in flight.

## Accessibility and localization
- The admin table is an MUI `Table` with `aria-label` from `access.tabs.requests`; the reject dialog is labelled via `aria-labelledby`.
- All visible text goes through i18n under the `accessRequests.*` namespace (`frontend/src/i18n/locales/`); status values are translated per-value (`accessRequests.statuses.<STATUS>`).
- Dates are localized with `toLocaleString(i18n.language)`.
- Destructive actions (Remove access) use a confirmation dialog before the call, consistent with other destructive actions in the app (`ConfirmDialog` pattern).
