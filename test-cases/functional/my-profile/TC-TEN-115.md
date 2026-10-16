# TC-TEN-115: My profile — replace and remove the photo

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-115 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-014, BR-PRF-021) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person with a photo.

## Steps
1. Upload a second picture, then read `GET /me/profile/photo`.
2. Remove the photo with the Remove button (`DELETE /me/profile/photo`).
3. Remove again when there is no photo.

## Expected Result
There is still one photo and it is the new one (its `ETag` and `updatedAt` changed). After removal `photo.present` is false, initials return and the completion drops by one item. A second removal is not an error. Audit has `PROFILE_PHOTO_REMOVED` once.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPhotoCanBeAddedReplacedAndRemovedWithAuditRecords`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `thePhotoIsUploadedServedWithItsHeadersAndRemoved`
- `frontend/src/pages/ProfilePage.test.tsx` — `removes the photo`

## Actual Result
Not run yet.

## Status
Not Run
