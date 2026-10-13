# TC-CAT-059: The banner image is stored and returned separately from the app icon and can be cleared

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-059 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C95) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
An app with an icon and a banner image.

## Steps
1. Create an app with an icon and a banner image. 2. Update it with a blank banner image.

## Expected Result
The response has both URLs after create; after the update the icon is unchanged and the banner is empty.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/product/service/ProductServiceTest.java` — `theBannerImageIsSavedAndReturnedSeparatelyFromTheAppIconAndCanBeCleared`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
