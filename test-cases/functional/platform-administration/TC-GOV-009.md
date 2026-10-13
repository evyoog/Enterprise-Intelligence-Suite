# TC-GOV-009: An administrator edits one language and resets it; each change is audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-009 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Edit the Spanish text of a type.
2. Read the type; read the English variant.
3. Reset Spanish.

## Expected Result
Only Spanish is marked customized; English is unchanged; after the reset Spanish equals the built-in text; audit entries `MESSAGE_TEMPLATE_CHANGED` and `MESSAGE_TEMPLATE_RESET` exist.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `anAdministratorEditsOneLanguageAndResetsItAndEachChangeIsAudited`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
