# TC-TEN-114: My profile — photo upload of a JPEG, PNG and WebP

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-114 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-013, BR-PRF-014) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person with no photo.

## Steps
1. Upload a valid JPEG, then a valid PNG, then a valid WebP through the screen (Change photo) and through `PUT /me/profile/photo`.
2. Look at the header avatar, the profile menu avatar and the completion figure.

## Expected Result
Each upload answers 200 with `photo.present` true and an `updatedAt`; the avatar shows the picture in the header and the menu; the missing list no longer contains `photo`; audit `PROFILE_PHOTO_UPDATED` is written.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aPhotoCanBeAddedReplacedAndRemovedWithAuditRecords`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfilePhotoInspectorTest.java` — `acceptsJpegPngAndWebpWhoseContentMatchesTheirType`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `thePhotoIsUploadedServedWithItsHeadersAndRemoved`
- `frontend/src/pages/ProfilePage.test.tsx` — `uploads the scaled photo and reports the new completion`
- `frontend/src/components/layout/AppShell.test.tsx` — `shows the person's photo in the top bar once the completion says there is one`

## Actual Result
Not run yet.

## Status
Not Run
