# TC-TEN-110: My profile — a non-administrator without a customer record gets 404

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-110 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-002) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person with a token but no customer row who is not a platform administrator.

## Steps
1. Call `GET /me/profile`, `PUT /me/profile`, `PUT /me/profile/photo`, `DELETE /me/profile/photo` and `GET /me/profile/photo`.
2. Call `GET /me/profile/completion`.
3. Open `/account/profile`.

## Expected Result
Each profile and photo call answers 404 and no customer row is created. The completion call answers 200 `{exists:false, percent:0, filled:0, total:11, missing:[all 11]}` (never 404), so the global bar stays hidden. The page shows "Could not load your profile." with a Retry button.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aNonAdminWithoutARecordGets404OnEveryOwnProfileCall`, `completionForASignedInPersonWithoutARecordIsExistsFalseNotAnError`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aNonAdminWithoutARecordGets404AndAPlatformAdminGetsExistsFalseThenCreatesTheRecord`
- `frontend/src/pages/ProfilePage.test.tsx` — `offers a retry when the profile cannot be loaded`

## Actual Result
Not run yet.

## Status
Not Run
