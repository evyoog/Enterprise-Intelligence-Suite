# TC-TEN-091: My profile — edit and save the Personal tab

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-091 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-007, BR-PRF-008, BR-PRF-012) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in member with a record.

## Steps
1. Open the Personal tab, change first name, last name, preferred name, mobile, alternate phone and bio.
2. Press Save, then reload the page.

## Expected Result
A success toast appears; after the reload the new values are shown (phones in their normalised `+` form), the header shows the preferred name as the display name, and `profile_updated_at` is set.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `savingPersistsEveryFieldNormalisedAndReturnsTheFullProfile`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `putSavesAndNormalisesAndTheCompletionEndpointFollows`
- `frontend/src/pages/ProfilePage.test.tsx` — `saves every tab in one request, normalised, then reports the new completion and shows a toast`, `shows the preferred name instead of the full name when one is set`

## Actual Result
Not run yet.

## Status
Not Run
