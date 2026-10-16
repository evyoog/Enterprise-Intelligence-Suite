# TC-TEN-099: My profile — address limits and the postal code pattern

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-099 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-010) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person with a non-India country.

## Steps
1. Save address lines of 150 characters, then 151; city and state of 80, then 81; country of 80, then 81.
2. Save postal codes `SW1A 1AA`, `10001`, `A1`, `12-345` and one of 11 characters.
3. Save postal codes `1` (one character), one of 12 characters, one of 13 characters, `-1234`, `12 345!`.

## Expected Result
The limits are accepted at the maximum and refused one over. Postal codes of 2 to 11 characters (the pattern limit; the column holds 12) that start with a letter or digit and hold only letters, digits, space and hyphen are accepted; the others are refused with `address.postalCode`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `workAndAddressLimitsAreEnforced`, `otherCountriesUseTheGeneralPostalPattern`
- `frontend/src/utils/profileValidation.test.ts` — `accepts general postal codes and rejects bad ones`, `limits the postal code to 12 characters`

## Actual Result
Not run yet.

## Status
Not Run
