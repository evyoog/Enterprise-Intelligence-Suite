# TC-GOV-014: Platform defaults start empty, are validated and are audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-014 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional / Negative |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Read the defaults.
2. Save a language, region, currency, time zone, date format and reminder days; clear one.
3. Save an unsupported language, a fake time zone, a disabled currency, a bad date format, reminder days 0 and 31.

## Expected Result
Step 1: all empty (reminder days shows the existing default). Step 2: saved and `PLATFORM_DEFAULTS_CHANGED` audited; the cleared field is empty. Step 3: each refused with a message, nothing saved.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/template/service/MessageTemplateAndDefaultsTest.java` — `platformDefaultsStartEmptyAreValidatedAndAudited`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
