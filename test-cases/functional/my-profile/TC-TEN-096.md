# TC-TEN-096: My profile — preferred name rules

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-096 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-007) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save a preferred name of 60 characters, then of 61 characters.
2. Save a preferred name containing a digit or `@`.
3. Leave it blank.

## Expected Result
60 characters are accepted; 61, a digit and `@` are refused with `personal.preferredName`; blank clears it and the header shows first and last name again.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `preferredNameFollowsTheSameCharacterRuleButIsOptional`, `nameLengthLimitsAreEnforced`
- `frontend/src/utils/profileValidation.test.ts` — `leaves the preferred name optional but applies the name pattern`, `rejects a name over 100 characters and a preferred name over 60`

## Actual Result
Not run yet.

## Status
Not Run
