# TC-PRT-019: Business dashboard — Resilience, motion and accessibility

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-019 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The audit log request fails once.

## Steps
1. Open the dashboard; click Retry; check motion helpers; run axe.

## Expected Result
Recent activity shows "unavailable" with Retry while the table still renders; Retry loads it; count-up runs once and is skipped with Reduce motion; no axe violations. Visual check at 1440, 1000 and 390 px: no horizontal overflow, no console errors.

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `a failing section shows Retry without breaking the rest of the dashboard`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `has no detectable accessibility violations`
- `frontend/src/components/dashboard/motion.test.tsx` — both tests

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
