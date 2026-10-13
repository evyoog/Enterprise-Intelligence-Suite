# TC-GOV-015: The defaults reach every user's preferences without overwriting their own choices

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-015 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Defaults set; a user with saved preferences and a new user.

## Steps
1. Read `/me/preferences` for both users.

## Expected Result
The `defaults` object is included for both; the user's own saved values are unchanged. In the app, `PreferenceSync` applies a default only to what the user has not chosen.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `theDefaultsReachEveryUsersPreferencesSoTheyStartWithThem`; `PreferenceServiceTest`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
