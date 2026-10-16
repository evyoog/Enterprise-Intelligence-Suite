# TC-TEN-097: My profile — phone normalisation and boundaries

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-097 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-008) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save mobile `+91 98765-43210`, `+1 (415) 555.2671` and alternate phone `+44 20 7946 0958`.
2. Save `+1234567` (7 digits) and `+123456789012345` (15 digits).
3. Save `+123456` (6 digits), `+1234567890123456` (16 digits), `9876543210` (no plus), `+0123456789`, `+91 98765 4321a`.
4. Submit a mobile of 41 characters made of digits and spaces.
5. Leave both blank.

## Expected Result
The formatted values are stored as `+919876543210`, `+14155552671` and `+442079460958`; 7 and 15 digits are accepted. 6 digits, 16 digits, no plus sign, a leading 0 country code and letters are refused with a `personal.mobile` or `personal.alternatePhone` error. Blank clears the phone. A value of 41 typed characters is refused even if its digits would fit.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `mobileIsNormalisedToE164`, `mobileMustBeInternationalFormat`, `alternatePhoneFollowsTheSameRule`, `aBlankPhoneIsSimplyEmpty`
- `frontend/src/utils/profileValidation.test.ts` — `strips spaces, hyphens, dots and parentheses from a phone number`, `applies the same rule to the alternate phone and leaves both optional`

## Actual Result
Not run yet.

## Status
Not Run
