# TC-CAT-026: Version and Updated on

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-026 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A published datasheet.

## Steps
1. Save it again without changes.
2. Rename it.
3. Replace its file.

## Expected Result
Step 1: version stays 1. Step 2: version 2. Step 3: version 3, new file name, the old object is deleted from storage, 'Updated on' changes. The product page shows 'Version 3 · updated {date}'.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `editingOrReplacingAFileRaisesTheVersionAndDeletesTheOldFile`, `theKindOfAnItemCannotChange`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
