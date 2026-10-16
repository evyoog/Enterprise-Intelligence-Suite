# TC-TEN-116: My profile — photo of a wrong type or with false content is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-116 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-013) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Upload an SVG (with and without a script), a GIF, a PDF, a text file.
2. Upload an executable or text file renamed `a.png` and sent as `image/png`.
3. Upload a real PNG sent with the header `image/jpeg`.
4. Upload a real JPEG named `a.svg` sent as `image/jpeg`.

## Expected Result
SVG, GIF, PDF, text, the disguised files and the PNG sent as JPEG are refused with 400 and the stored photo is unchanged (the declared type must be allowed and must equal the type found from the first bytes). The real JPEG named `a.svg` is accepted: the file name is ignored.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfilePhotoInspectorTest.java` — `refusesSvgGifAndAnyOtherDeclaredType`, `theMagicBytesDecideNotTheHeader`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aWrongTypeOversizeOrDisguisedPhotoIsRefusedAndChangesNothing`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `badPhotosAreRefusedWithTheRightStatus`
- `frontend/src/utils/profilePhoto.test.ts` — `refuses SVG, GIF, PDF and an unknown type`

## Actual Result
Not run yet.

## Status
Not Run
