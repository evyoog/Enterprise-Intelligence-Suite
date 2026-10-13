# TC-GOV-008: The built-in message text is what EIS sent before, in the recipient's language

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-008 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
No template edited.

## Steps
1. Render each of the 14 message types in English and Spanish with example values.

## Expected Result
Every type renders; placeholders are filled; the English text equals the text EIS sent before templates existed; Spanish is available for every type.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `theBuiltInTextIsWhatTheCodeSentBeforeAndFillsPlaceholdersInTheChosenLanguage`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
