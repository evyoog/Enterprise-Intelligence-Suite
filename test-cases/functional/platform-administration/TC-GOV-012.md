# TC-GOV-012: Notifications and emails use the edited text in the recipient's language

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-012 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An edited template; a recipient whose language is Spanish and one whose language is English.

## Steps
1. Trigger a notification of that type for each recipient.
2. Trigger an email of an edited email type.

## Expected Result
Each recipient gets the edited text in their own language; with no edit the built-in text is used.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `notificationsAndEmailsUseTheEditedTextInTheRecipientsLanguage`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
