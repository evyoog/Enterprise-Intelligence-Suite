# TC-TEN-098: My profile — bio and work field limits

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-098 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-009) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save a bio of 500 characters, then 501.
2. Save job title and department of 100 characters, then 101.
3. Save company name of 150 characters, then 151.
4. Save industry of 100 characters, then 101.

## Expected Result
The maximum lengths are accepted; one more character is refused with the field error `bio`, `work.jobTitle`, `work.department`, `work.companyName` or `work.industry`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `bioIsLimitedTo500Characters`, `workAndAddressLimitsAreEnforced`
- `frontend/src/utils/profileValidation.test.ts` — `counts length after trimming and collapsing`

## Actual Result
Not run yet.

## Status
Not Run
