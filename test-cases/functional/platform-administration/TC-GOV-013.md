# TC-GOV-013: The language of a message is the recipient's, else the platform default, else English

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-013 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A recipient with a saved language; one without; no platform default; then a platform default.

## Steps
1. Resolve the language for each case.

## Expected Result
Recipient's own language first; otherwise the platform default language; otherwise English; an unknown recipient gets the platform default, else English.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `theLanguageIsTheRecipientsOwnThenThePlatformDefaultThenEnglish`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
