# TC-CAT-021: A mismatching or missing upload is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-021 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-CAT-019.

## Steps
1. Upload an object whose size differs from the declared size, then save the item.
2. Upload an object with another content type, save.
3. Save an item whose key was never uploaded, a key of another application, a key with `..`, a reused key.

## Expected Result
400 `UPLOAD_MISMATCH` (the object is deleted), `UPLOAD_NOT_FOUND`, `UPLOAD_MISMATCH` and 409 `DUPLICATE_UPLOAD`; no item is created.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `anUploadThatDoesNotMatchWhatWasDeclaredIsRefusedAndDeleted`, `aMissingObjectAForeignKeyAndAReusedKeyAreRefused`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
