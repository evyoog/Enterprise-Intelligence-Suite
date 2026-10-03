# TC-PRT-015: Business dashboard — Header, welcome, status and real KPIs

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-015 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Org Acme Corp, 10 licensed seats, 4 active, 2 applications (42 launches).

## Steps
1. Open the dashboard.

## Expected Result
Organization name, greeting with the user's real name, "All systems operational" from service status, "6 unused seats" → members; quick actions use only existing routes; KPIs 10 / 4 / 2 / 42 with "40% used" and no invented trends.

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `shows the organization, a greeting with the real user name, real status and seats`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `derives the KPIs from the business dashboard (no invented trends)`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
