# TC-SRM-010: An organization administrator sees only their own organization's instances, read-only

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-010 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-013](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two organizations each with an instance of one product; an administrator and an ordinary member of the first.

## Steps
1. The administrator lists the organization's instances.
2. The member tries the same.
3. Open Organization → Service instances; open it with no instances; open it when the API fails.

## Expected Result
Step 1: only the first organization's instance. Step 2: refused. Step 3: a read-only table with the access URL as a link and no action; an empty state "No service instances yet"; an error with a retry.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `anOrganizationAdminSeesOnlyTheirOrganizationsInstancesAndAMemberCannot`
- `frontend/src/pages/OrganizationServiceInstancesPage.test.tsx` — "lists the organization instances read-only, with the access URL as a link, and has no accessibility violations", "shows an empty state when there are no instances", "shows the error and retries"

Not covered by an automated test: that a DEPROVISIONED instance is left out of the organization list, and the exact 403 status of the member's refusal (the test expects a runtime exception).

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
