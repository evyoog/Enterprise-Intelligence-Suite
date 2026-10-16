# TC-TEN-093: My profile — trim, collapse spaces, blank becomes empty

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-093 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-005) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save first name `  Ana   Maria  `, city `   `, department `Quality     Assurance`.
2. Save a blank first name.

## Expected Result
Stored values are `Ana Maria`, no city (null) and `Quality Assurance`. The blank first name is refused with a `personal.firstName` field error and nothing is saved.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `textIsTrimmedAndInnerWhitespaceCollapsed`, `firstAndLastNameAreRequiredAndBlankCountsAsMissing`
- `frontend/src/utils/profileValidation.test.ts` — `trims and collapses inner whitespace, newlines and tabs`
- `frontend/src/pages/ProfilePage.test.tsx` — `tidies what was typed when a field is left: phone separators and extra spaces`

## Actual Result
Not run yet.

## Status
Not Run
