# TC-TEN-100: My profile — India postal code is exactly 6 digits

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-100 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-010) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Set country `India`, then `india`, then `IN`, then `in`; save postal code `560001` each time.
2. With each of those countries save `56001`, `5600011`, `56000A`, `560 001`.
3. Set country `Indonesia` and save `56001`.

## Expected Result
`560001` is accepted for every spelling of India. The four wrong codes are refused with `address.postalCode` while the country is India. For `Indonesia` the generic pattern applies and `56001` is accepted.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `indiaPostalCodeMustBeExactlySixDigits`
- `frontend/src/utils/profileValidation.test.ts` — `requires exactly 6 digits when the country is India (India or IN, any case)`, `re-evaluates the postal code when the country changes`
- `frontend/src/pages/ProfilePage.test.tsx` — `applies the Indian postal code rule using the country on the same tab`

## Actual Result
Not run yet.

## Status
Not Run
