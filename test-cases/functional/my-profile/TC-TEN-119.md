# TC-TEN-119: My profile — the photo is read only by its owner, with the right headers

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-119 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-014, BR-PRF-001) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Persons A (with a photo) and B (without), both signed in.

## Steps
1. As A call `GET /me/profile/photo`; check `Content-Type`, `Cache-Control` and `ETag`.
2. Repeat with `If-None-Match` set to the ETag.
3. Repeat with `If-None-Match` set to a different value.
4. As B call `GET /me/profile/photo`.
5. Read A's `GET /me/profile` JSON and look for image data.

## Expected Result
A receives the bytes with the stored content type, `Cache-Control: private, max-age=0, must-revalidate`, an `ETag` and `X-Content-Type-Options: nosniff`; a matching `If-None-Match` gives 304 without a body and a different one gives 200. B gets 404, never A's picture. The profile JSON holds only `photo.present` and `photo.updatedAt`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `thePhotoIsUploadedServedWithItsHeadersAndRemoved`, `aPhotoIsNeverServedToAnotherPerson`, `getReturnsTheCallersOwnProfileWithExplicitNulls`
- `frontend/src/api/profileApi.test.ts` — `reads the photo bytes, or null on 404`

## Actual Result
Not run yet.

## Status
Not Run
