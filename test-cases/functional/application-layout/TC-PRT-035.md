# TC-PRT-035: Regular member sees the short menu, with no admin module

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-035 |
| Requirement ID (required) | [C80](../../../docs/01-business/roadmap/open-decisions.md#c80) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in individual or organization member with no management permission.

## Steps
1. Open My applications.
2. Read the sidebar group labels and links.
3. Open Knowledge Center, then Guides.

## Expected Result
Groups: Workspace, Help, Billing, Account. Items: My applications, Product catalog, Knowledge Center (children Articles, Guides, FAQs once inside it), Support, Overview, Invoices & payments, Security, Preferences. No Organization, Platform or Operations group, no Service status, Partners, Audit log, Integrations, Products or Applications. Support appears once. Guides is marked current and Knowledge Center is one entry.

## Automated coverage
- `frontend/src/components/layout/appNavigation.test.ts` — `regular member (C80 §3)`
- `frontend/src/components/layout/AppShell.test.tsx` — `shows a regular member the short menu…`, `makes Knowledge Center one clickable entry…`

## Actual Result
The automated tests above passed on 2026-10-06. Looking at it in a running browser is manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
