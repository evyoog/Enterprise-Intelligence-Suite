# TC-PRT-033: Sign out opens the website home page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-033 |
| Requirement ID (required) | [C79](../../../docs/01-business/roadmap/open-decisions.md#c79) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in user.

## Steps
1. On a page reachable signed out (for example `/products` or `/knowledge`), click **Sign out** in the sidebar.
2. Sign in again, open the account menu and choose **Sign out**.
3. Sign in again, open `/register/verify` or the website via Home, and click **Sign out** in the website header (and in its mobile menu).

## Expected Result
Each time the session ends and the website landing page `/` opens for a visitor (Login and Get started shown).

## Automated coverage
- `frontend/src/components/layout/AppShell.test.tsx` — `opens the website home page after signing out, from any page (C79)`, `offers account pages and sign out from the user menu`
- `frontend/src/components/layout/SiteNavbar.test.tsx` — `signing out opens the website home page (C79)`

## Actual Result
The automated tests above passed on 2026-10-06.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
