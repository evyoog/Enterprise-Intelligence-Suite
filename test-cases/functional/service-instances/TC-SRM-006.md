# TC-SRM-006: Status page derivation: more than half Down is a major outage

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-006 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-010](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with four reporting instances and a reporter key.

## Steps
1. All four report HEALTHY.
2. One reports DEGRADED.
3. One reports DOWN.
4. A second reports DOWN (exactly half).
5. A third reports DOWN.

## Expected Result
The derived status is Operational, Degraded, Partial outage, Partial outage (half is not more than half), then Major outage with 3 down of 4 reporting.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `moreThanHalfDownIsAMajorOutageSomeDownIsPartialDegradedIsDegradedAndAllHealthyIsOperational`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
