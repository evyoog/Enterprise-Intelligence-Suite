# TC-PRT-017: Business dashboard — Your account, workspace and activity

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-017 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Signed in as ada@acme.example (Organization admin, MFA off, org requires MFA, 1 SAML provider).

## Steps
1. Open the dashboard; remove a favourite.

## Expected Result
Account shows role, email, Active, "Not enabled", "Required by your organization", "Connected (1 provider)", permissions; recent activity labels audit actions; removing the favourite calls the API and shows the empty state.

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `shows the signed-in user from the member list, permissions and identity`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `lists recent activity and favourites, and can remove a favourite`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
