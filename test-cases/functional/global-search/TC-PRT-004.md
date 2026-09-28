# TC-PRT-004: The type filter narrows to one category

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-004 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE product with a distinctive name.

## Steps
1. Arrange: create an ACTIVE product.
2. Act: call search(name, "KNOWLEDGE", null).
3. Act: call search(name, "PRODUCT", null).
4. Observe both results.

## Expected Result
Step 2's products list is empty; step 3's products list includes the product.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/GlobalSearchServiceTest.java` — `typeFilterNarrowsToOneCategory`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
