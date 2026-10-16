# TC-TEN-092: My profile — save the Work, Address and Online tabs; blank clears

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-092 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-009, BR-PRF-010, BR-PRF-011, BR-PRF-012) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person whose record has job title, city and website filled.

## Steps
1. Fill job title, department, company name, industry, both address lines, city, state, postal code, country, LinkedIn and website; save.
2. Clear the job title and the website and save again (full replace).
3. Reload.

## Expected Result
The first save stores every value; after the second save job title and website are empty (null) and every other value is unchanged.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aSaveIsAFullReplaceSoOmittedFieldsAreCleared`, `savingPersistsEveryFieldNormalisedAndReturnsTheFullProfile`
- `frontend/src/pages/ProfilePage.test.tsx` — `saves every tab in one request, normalised, then reports the new completion and shows a toast`

## Actual Result
Not run yet.

## Status
Not Run
