# TC-SUB-043: Plans are compared per month, so a yearly plan can be a downgrade

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-043 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) (.8; BR-SUB-011) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A monthly plan at 100 and a yearly plan at 1,000 (83.33 a month).

## Steps
1. Ask whether the yearly plan is a downgrade from the monthly plan, and the reverse.

## Expected Result
Monthly to yearly: a downgrade. Yearly to monthly: not a downgrade.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/ScheduledPlanChangeTest.java` — `aYearlyPlanIsComparedPerMonth`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
