# TC-GOV-011: A preview shows example values and saves nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-011 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Preview edited text.
2. Read the type again.

## Expected Result
The preview has the placeholders replaced by example values; the stored text is unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `aPreviewShowsExampleValuesAndSavesNothing`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
