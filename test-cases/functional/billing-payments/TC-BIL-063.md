# TC-BIL-063: The reminder kind depends on the days left to the due date

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-063 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.27; BR-17) |
| Acceptance Criterion | [AC-39](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An invoice due on a fixed date.

## Steps
1. Ask which reminder applies 10, 3, 1 and 0 days before, and 3 and 7 days after the due date.

## Expected Result
None more than 3 days before; BEFORE_DUE 3 to 1 days before; ON_DUE on the due date; AFTER_DUE from 7 days after.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/InvoiceReminderServiceTest.java` — `theKindDependsOnTheDaysLeftToTheDueDate`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
