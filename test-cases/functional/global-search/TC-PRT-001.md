# TC-PRT-001: A matching active product appears in results

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-001 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE product with a distinctive name.

## Steps
1. Arrange: create an ACTIVE product with a unique name.
2. Act: call search(name, null, null).
3. Observe the products list.

## Expected Result
The product appears in the products list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/GlobalSearchServiceTest.java` — `findsAMatchingProduct`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
