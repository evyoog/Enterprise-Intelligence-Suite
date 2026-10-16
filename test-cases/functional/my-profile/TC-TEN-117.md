# TC-TEN-117: My profile — photo size boundaries

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-117 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-013) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Upload a valid image of exactly 1,048,576 bytes.
2. Upload a valid image of 1,048,577 bytes.
3. Upload an empty file and a request without the `file` part.

## Expected Result
1,048,576 bytes is accepted. 1,048,577 bytes is refused with 413 (the screen also refuses it before sending). The empty file and the missing part are refused with 400.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfilePhotoInspectorTest.java` — `refusesEmptyAndOversizeFiles`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aWrongTypeOversizeOrDisguisedPhotoIsRefusedAndChangesNothing`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `badPhotosAreRefusedWithTheRightStatus`
- `frontend/src/pages/ProfilePage.test.tsx` — `explains a wrong type or an oversized result without calling the server`

## Actual Result
Not run yet.

## Status
Not Run
