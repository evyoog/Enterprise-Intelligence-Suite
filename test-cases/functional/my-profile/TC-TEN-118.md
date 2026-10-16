# TC-TEN-118: My profile — photo dimensions, damaged images and browser downsizing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-118 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-013) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Upload a valid image of 4096 x 4096 and one of 4097 x 4096 (under 1 MB).
2. Upload a file with valid PNG first bytes but damaged data (a truncated PNG).
3. On the screen choose a 3000 x 2000 photo and a 200 x 200 photo and observe the request body.

## Expected Result
4096 x 4096 is accepted; 4097 wide and the damaged PNG are refused with 400 (a WebP is checked by its header and declared size only). The screen sends a centred square of at most 512 x 512 pixels (a smaller picture is not enlarged), as WebP or JPEG within 1 MB.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfilePhotoInspectorTest.java` — `refusesImagesLargerThan4096PixelsInEitherDirection`, `refusesACorruptImageThatOnlyHasTheRightFirstBytes`, `readsTheDimensionsOfLossyAndLosslessWebpHeaders`, `aWebpContainerWithAnUnknownChunkIsRefused`
- `frontend/src/utils/profilePhoto.test.ts` — `cuts a centred square from a landscape image and scales to 512`, `never upscales a small image`, `crops to a centred square, scales to 512 and returns a WebP of at most 1 MB`

## Actual Result
Not run yet.

## Status
Not Run
