# TC-SUB-056: People with Manage billing are always-on reminder recipients

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-056 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-8](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An organization with an administrator and a plain member who is given the Manage billing permission; a subscription two days before its renewal.

## Steps
1. Open the Renewal reminder recipients section as the administrator.
2. Run the reminder job two days before the renewal and check the Manage billing member's notifications and email.

## Expected Result
Step 1: the Manage billing member is listed under "Always receive reminders" with the chip "Manage billing", not editable, and does not appear in the candidates. Step 2: they receive the renewal reminder (and, later, the grace reminder) on both channels.

## Automated coverage
- `frontend/src/components/subscriptions/RenewalRecipientsSection.test.tsx` — "lists the default recipients read-only with their reasons, and the added people with their channels" (the screen, with a mocked API that returns a Manage billing person)

Not covered by an automated test: the backend rule that a member with `MANAGE_ORGANIZATION_BILLING` is a default recipient (`RenewalReminderService.recipients`, `RenewalReminderRecipientService.build`); the recipient test only uses an administrator. **Not Run**.

## Actual Result
The automated screen test passed on 2026-10-14. The backend step has not been run.

## Status
Passed for the automated part (2026-10-14); the backend part is Not Run

## Linked Defect (if failed)
None.
