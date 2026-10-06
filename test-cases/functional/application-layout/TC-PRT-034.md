# TC-PRT-034: No customer data is visible after sign-out

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-034 |
| Requirement ID (required) | [C79](../../../docs/01-business/roadmap/open-decisions.md#c79) (follow-up 2026-10-06) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P0 |
| Type | Functional / Security |
| Automated | Yes (manual steps 4 and 5) |

## Preconditions
A signed-in organization user with items in the cart, who has searched the Knowledge Center and has subscriptions and invoices.

## Steps
1. Open a page with the user's data (for example My subscriptions), then sign out.
2. Sign in again and open a restricted Knowledge Center article. In a second tab, sign out.
3. Sign in, return to the browser window (focus) and sign out at once.
4. After signing out, press the browser's Back button several times.
5. As a visitor on the same browser, open the Knowledge Center search.

## Expected Result
1. The website home page opens as for a visitor (Login, Get started); no name, cart count, subscription or invoice is shown.
2. Within the session check interval (20 s, or at once when the first tab gets focus) the first tab also opens the website home page.
3. The user stays signed out; the session is not restored by a check that was already running.
4. Pages that need sign-in send to the home page; public pages show only public content. No page is shown from the browser cache (API answers carry `Cache-Control: no-store`).
5. No recent searches from the previous user are listed.

## Automated coverage
- `frontend/src/auth/AuthProvider.test.tsx` — `a session check already running when the user signs out cannot sign them back in` (fails on the previous code)
- `frontend/src/auth/SignedOutRedirect.test.tsx` — `opens the home page and forgets recent searches when a session ends anywhere`, `leaves a visitor who was never signed in where they are`
- `frontend/src/components/layout/AppShell.test.tsx` — `opens the website home page after signing out, from any page (C79)`
- `backend/src/test/java/com/vyoog/eisplatform/config/NoCacheHeadersTest.java` — signed-in and refused API answers carry `no-store`

## Actual Result
The automated tests above passed on 2026-10-06. Steps 4 and 5 in a real browser are manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
