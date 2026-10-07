# TC-CAT-030: Deleting content and applications removes the files

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-030 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An application with a case study (PDF and logo) and an image.

## Steps
1. Delete the case study.
2. Delete the application.

## Expected Result
The case study's objects are deleted from storage, then the image's object and every row go with the application.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `deletingAnItemDeletesItsFilesAndDeletingTheProductDeletesEverything`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
