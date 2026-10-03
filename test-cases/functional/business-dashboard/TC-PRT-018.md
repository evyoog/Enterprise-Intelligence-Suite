# TC-PRT-018: Business dashboard — Attention, status and billing from real data

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-018 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Backend alert UNDERUTILIZED_SEATS; a renewal on 30 Oct; spend 150.00 vs 100.00 USD; then no alerts; then a degraded application.

## Steps
1. Open the dashboard in each state.

## Expected Result
Only the real alert is listed with "Manage members"; billing shows the next renewal and 150.00 USD with 50.0%; with no alerts "You're all caught up"; a degraded application shows "Degraded performance" and never "All systems operational".

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `shows only real attention items, each with its action, and the billing summary`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `says you're all caught up when nothing needs attention`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `reports a degraded service instead of "operational"`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
