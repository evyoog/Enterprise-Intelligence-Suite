# TC-TEN-155: My profile — logs never contain file content or emails

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-155 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-031) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Security |
| Automated | No (manual check) |

## Preconditions
A running backend with the registry enabled; a source that loads, then fails to refresh.

## Steps
1. Read the INFO line at start and after a content change.
2. Break the source and read the WARN line.
3. Search the log for the emails and ids in the file.

## Expected Result
The INFO line carries only the number of enabled entries and the source type; the WARN line only the source type and the error class. No email address, `keycloakSub`, display name, notes or file text appears in any log line.

## Automated coverage
- None: manual check (no test asserts log content).

## Actual Result
Not run yet.

## Status
Not Run
