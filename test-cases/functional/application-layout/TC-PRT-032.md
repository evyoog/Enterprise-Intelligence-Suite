# TC-PRT-032: Home button opens the website landing page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-032 |
| Requirement ID (required) | [C79](../../../docs/01-business/roadmap/open-decisions.md#c79) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in organization user on any tool page (for example `/products`).

## Steps
1. Click **Home** in the top bar.
2. Reload the page.
3. Sign out, then open the website header as a visitor.

## Expected Result
1. The website landing page `/` opens with the website header ("My Workspace" and Sign out); the tool sidebar is not shown.
2. The landing page is still shown after the reload.
3. The website header has a **Home** link to `/` (also first in the mobile menu). Typing `/` while signed in, without using Home, still opens the tool home.

## Automated coverage
- `frontend/src/components/layout/AppShell.test.tsx` — `shows the website home page to a signed-in user who clicks Home (C79)`, `sends a signed-in organization user from "/" into the tool…`
- `frontend/src/components/layout/SiteNavbar.test.tsx` — `has a Home link to the website home page (C79)`

## Actual Result
The automated tests above passed on 2026-10-06. Step 2 (reload) is manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
