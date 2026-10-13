# TC-GOV-010: A template with an unknown or missing placeholder or too much text is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-010 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Negative |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Save text using a placeholder the type does not offer.
2. Save text that lacks a required placeholder.
3. Save a subject over 200 or a body over 5000 characters, and an unsupported language.

## Expected Result
Each save is refused with a message naming the problem; nothing is stored.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `aTemplateMustUseOnlyKnownPlaceholdersKeepTheRequiredOnesAndStayWithinTheLimits`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
