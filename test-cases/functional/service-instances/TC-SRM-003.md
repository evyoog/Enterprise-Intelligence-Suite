# TC-SRM-003: A product reports health with its key; the first report activates the instance

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-003 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-005, BR-SRM-006, BR-SRM-007](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with a reporter key and a PROVISIONING instance of its own.

## Steps
1. Report `degraded` with the message "Slow queue", tenant reference "tenant-1" and an access URL.
2. Send a call with one valid report (HEALTHY) and one invalid (BROKEN).

## Expected Result
Step 1: accepted = 1; the instance is ACTIVE, health DEGRADED, tenant reference stored, last changed by PRODUCT. Step 2: accepted = 1, rejected = 1, and the error starts "Report 2".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `aProductReportsHealthWithItsKeyAndTheFirstReportActivatesTheInstance`, `anInvalidHealthValueIsRejectedPerReport`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
