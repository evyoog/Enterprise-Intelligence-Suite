# TC-CAT-019: Upload URL for a datasheet

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-019 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An application exists; a platform administrator (MANAGE_CATALOG).

## Steps
1. Ask for an upload URL for a 1 MB PDF named `../../Q3 datasheet.pdf`.

## Expected Result
A presigned PUT URL for a new key `product-content/{id}/datasheet/{uuid}.pdf` (the file name is not in the key), valid 15 minutes, maximum 20 MB.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `uploadUrlIsForANewKeyUnderTheProductWithAShortExpiry`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
