# TC-GOV-016: Templates and defaults need Manage platform settings

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-016 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A signed-in user without `MANAGE_PLATFORM_SETTINGS`; one with it.

## Steps
1. Call each templates and defaults endpoint without, then with, the permission.

## Expected Result
Without: 403 on every endpoint. With: 200.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/MessageTemplateAuthorizationTest.java`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
