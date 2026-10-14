# TC-SRM-007: A stale report is left out and the manual status is kept

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-007 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-008, BR-SRM-010](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with one instance that reported DOWN; an administrator has posted DEGRADED for the product.

## Steps
1. Make the instance's last report 10 minutes old.
2. Read the derived status and the admin status page.
3. Open the Instances tab.

## Expected Result
The derivation returns nothing; the product shows the administrator's DEGRADED with source MANUAL; the instance is flagged stale ("No recent report").

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `aStaleReportIsIgnoredAndTheManualStatusIsKept`
- `frontend/src/pages/admin/AdminServiceInstancesPage.test.tsx` — "shows the summary, the instances and the reporter keys, and has no accessibility violations"

The "No recent report" label of the table is shown in the frontend test data; the 5-minute cut-off itself is tested in the backend.

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
