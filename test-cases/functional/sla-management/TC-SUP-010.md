# TC-SUP-010: A warning goes out once at 80% to the assignee and the alert list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-010 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-009, BR-SLA-011, BR-SLA-012](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An URGENT ticket assigned to agent A; person B (with an account) on the alert list; check run with a later "now".

## Steps
1. Run the check 100 minutes after creation.
2. Run it 400 minutes after creation (80% of 480 is 384).
3. Run it again 410 minutes after creation.

## Expected Result
Step 1: no warning. Step 2: the resolution warning is stored, the ticket is not escalated, A and B each get an in-app notice "Support ticket nearing its resolution target", the state is WARNING. Step 3: nobody is told again.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `aWarningGoesOutOnceAtTheWarningPointToTheAssigneeAndTheAlertList`
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaCalculatorTest.java` — `theWarningPointIsAPercentageOfTheTarget`

Not covered by an automated test: an alert address that has no account (email only).

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
