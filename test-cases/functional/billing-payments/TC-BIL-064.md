# TC-BIL-064: Each reminder is sent once and stops when the invoice is paid

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-064 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.27; BR-17) |
| Acceptance Criterion | [AC-40](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN invoice approaching its due date; another that is PAID.

## Steps
1. Run the reminder job on consecutive days around the due date.
2. Pay the invoice and run it again.
3. Run it for the paid invoice.

## Expected Result
One reminder per kind, never repeated; nothing after payment; nothing for a paid invoice.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/InvoiceReminderServiceTest.java` — `eachReminderIsSentOnceAndStopsWhenTheInvoiceIsPaid`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/InvoiceReminderServiceTest.java` — `aPaidInvoiceGetsNoReminder`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
