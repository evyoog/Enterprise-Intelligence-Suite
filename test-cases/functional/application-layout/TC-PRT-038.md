# TC-PRT-038: Old URLs redirect and merged pages work as tabs

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-038 |
| Requirement ID (required) | [C80](../../../docs/01-business/roadmap/open-decisions.md#c80) — [application layout](../../../docs/05-ui/screen-requirements/application-layout.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in user who may open the target pages.

## Steps
1. Open /admin/settings, /admin/settings/product, /admin/settings/platform, /admin/settings/common, /admin/permissions, /admin/service-status.
2. Open /organization/settings, then /organization/settings#groups, #mfa, #privileged-access.
3. Use the tabs on Members, Sign-in security, Products, Roles & permissions, Service status and Billing; reload each.

## Expected Result
Step 1 opens Applications with the Add dialog, the Add product page, Products → Configuration, Roles & permissions → Permissions and Service status → Manage. Step 2 opens Members, Members → Groups, Sign-in security → Multi-factor policy and Privileged access. Step 3: each tab shows its content, the selected tab is in the URL and survives a reload; tabs the user has no permission for are not shown.

## Automated coverage
- `frontend/src/pages/SidebarConsolidation.test.tsx` (all)
- `frontend/src/components/layout/AppShell.test.tsx` — `shows breadcrumbs on a nested page`

## Actual Result
The automated tests above passed on 2026-10-06. Looking at it in a running browser is manual.

## Status
Passed (automated run 2026-10-06)

## Linked Defect (if failed)
