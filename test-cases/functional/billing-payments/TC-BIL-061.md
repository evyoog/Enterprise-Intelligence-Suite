# TC-BIL-061: Pay by invoice starts an enabled organization's purchase at once; a not-enabled organization is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-061 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.25, .28; BR-13, BR-18) |
| Acceptance Criterion | [AC-37](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Organization A enabled for Pay by invoice and organization B not enabled, each with a pending subscription and an OPEN invoice.

## Steps
1. As A, choose Pay by invoice.
2. As B, choose Pay by invoice.

## Expected Result
A: the subscription is ACTIVE and the result carries a due date. B: refused (403) and the subscription is still PENDING_SUBSCRIPTION.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PendingActivationTest.java` — `payByInvoiceStartsAnApprovedOrganizationsPurchaseAtOnceWithADueDate`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/PendingActivationTest.java` — `anOrganizationNotEnabledForPayByInvoiceStaysPendingUntilItPays`

Not covered by an automated test: the approved organization order path (the subscription is created ACTIVE by `subscribeOrganization`): check manually by approving an order.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
