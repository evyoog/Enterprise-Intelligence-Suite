# TC-BIL-060: An online purchase stays pending until the invoice is paid, then starts once

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-060 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.25; BR-12, BR-14) |
| Acceptance Criterion | [AC-36](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with a subscription in PENDING_SUBSCRIPTION and its OPEN invoice.

## Steps
1. Read the subscription.
2. Mark the invoice paid.
3. Read the subscription again, note `startedAt`.
4. Deliver the same payment again (a repeated webhook).

## Expected Result
Step 1: PENDING_SUBSCRIPTION. Step 3: ACTIVE with a start date. Step 4: the start date is unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PendingActivationTest.java` — `anOnlinePurchaseStaysPendingUntilTheInvoiceIsPaidThenStartsOnce`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
