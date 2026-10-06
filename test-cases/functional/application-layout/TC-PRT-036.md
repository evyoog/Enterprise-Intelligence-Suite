# TC-PRT-036: Administrator menus (organization administrator and platform administrator)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-036 |
| Requirement ID (required) | [C80](../../../docs/01-business/roadmap/open-decisions.md#c80) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
(a) A user holding MANAGE_ORGANIZATION. (b) A platform administrator.

## Steps
1. Sign in as (a), read the sidebar.
2. Sign in as (b), read the sidebar.
3. Look for Settings, Knowledge Management, Search and a second Service status or Support.

## Expected Result
(a) Workspace, Organization (Members, Privileged access, Sign-in security, Orders), Platform (Service status only), Operations (Billing, Support), Account. (b) Workspace (Dashboard, Product catalog, Knowledge Center with Articles, Categories, Drafts, Manage), Organization (Roles & permissions, Registrations, Privileged access), Platform (Products, Applications, Integrations, Search, Service status), Operations (Billing with Invoices & payments, Payment gateway, Billing settings; Support; Reviews; Partners; Audit log), Account. No Settings, Knowledge Management, sidebar Search or duplicates.

## Automated coverage
- `appNavigation.test.ts` — `organization admin (C80 §2)`, `platform administrator (C80 §2)`
- `AppShell.test.tsx` — `shows an organization admin Organization, Platform (status only) and Operations with Support once`

## Actual Result
The automated tests above passed on 2026-10-06. Looking at it in a running browser is manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
