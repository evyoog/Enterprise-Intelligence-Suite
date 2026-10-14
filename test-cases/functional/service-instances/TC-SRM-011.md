# TC-SRM-011: The Instances tab shows the summary, the table and the filters

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-011 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-013, BR-SRM-014](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator; instances of several products, statuses and healths, one with no recent report.

## Steps
1. Open Platform → Service status → Instances.
2. Filter by product, then status, then health.
3. Open it with no instances and when the API fails.

## Expected Result
The summary shows Instances, Healthy, Degraded, Down and No recent report; the table shows each instance; each filter narrows the table; the empty state and the error with a retry appear; the tab has no axe violations.

## Automated coverage
- `frontend/src/pages/admin/AdminServiceInstancesPage.test.tsx` — "shows the summary, the instances and the reporter keys, and has no accessibility violations", "filters by product, status and health", "shows an empty state and an error with retry"

The screen is automated. **Not covered by an automated test:** the backend listing, the product/status/health filters and the summary counts (`listAll`, `summary`); run them by hand against `GET /admin/service-status/instances` and `…/summary`.

## Actual Result
The frontend tests passed on 2026-10-14. The backend listing and summary were not run by a test.

## Status
Partly passed: frontend automated run 2026-10-14; backend listing and summary Not Run

## Linked Defect (if failed)
None.
