# TC-TEN-095: My profile — first and last name rules at their boundaries

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-095 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-007) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save names of 1 character and of exactly 100 characters.
2. Save a name of 101 characters.
3. Save names with an accent (`José`), an apostrophe (`O'Brien`, `O’Neil`), a hyphen (`Anne-Marie`), a full stop (`Jr.`), and a non-Latin letter (`李`).
4. Save names that start with a space-collapsed blank, a digit (`4na`), a hyphen (`-Ana`), or contain `_`, `@` or an emoji.

## Expected Result
1 and 100 characters and the accented, apostrophe, hyphen, full-stop and non-Latin names are accepted. 101 characters, a leading digit or hyphen, `_`, `@` and emoji are refused with a field error on the right name field. A first and last name can never be saved blank.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `namesAcceptLettersMarksSpacesAndPunctuation`, `namesRejectDigitsSymbolsAndABadFirstCharacter`, `nameLengthLimitsAreEnforced`, `firstAndLastNameAreRequiredAndBlankCountsAsMissing`
- `frontend/src/utils/profileValidation.test.ts` — `accepts a normal record`, `requires first and last name; blank counts as missing`, `rejects a name over 100 characters and a preferred name over 60`

## Actual Result
Not run yet.

## Status
Not Run
