# TC-TEN-044: Organizations directory — organization actions and shell

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-044 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. On the detail page edit, suspend, activate, close and change seats. 2. As a plain member check the endpoints answer 403. 3. Open the profile menu; check the sidebar has no Account group and an organization admin sees People & structure.

## Expected Result
Lifecycle actions behave as in REQ-TEN-001; the directory endpoints are limited to MANAGE_REGISTRATIONS (security configuration); Security and Preferences are in the profile menu; Structure is a tab of People & structure.

## Automated coverage
- `frontend/src/pages/admin/AdminOrganizationDetailPage.test.tsx` — edit, suspend, refusal and closed-organization tests
- `frontend/src/components/layout/AppShell.test.tsx`, `appNavigation.test.ts`, `frontend/src/pages/OrganizationMembersPage.test.tsx`
- Access control: manual (the `/admin/organizations/**` rule in `SecurityConfig`)

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
