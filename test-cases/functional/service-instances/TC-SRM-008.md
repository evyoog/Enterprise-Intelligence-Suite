# TC-SRM-008: The status page uses the instances, says how many, and Maintenance wins

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-008 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-011, BR-SRM-012](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with one instance that just reported DOWN.

## Steps
1. Open the status page (admin and customer view).
2. An administrator posts Maintenance for the product.

## Expected Result
Step 1: Major outage, source INSTANCES, 1 down instance; the customer page says "Based on 1 reporting instance (1 down, 0 degraded)". Step 2: Maintenance, source MANUAL.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `theStatusPageUsesTheInstancesButMaintenanceWins`
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "shows how a status was worked out when it is based on instance reports"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
