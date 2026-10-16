# TC-TEN-124: My profile — completion calculation and rounding

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-124 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-017) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person with no completion items filled.

## Steps
1. Fill the items one at a time in the order photo, mobile, job title, department, company name, industry, address line 1, city, state, postal code, country and read the percentage after each.

## Expected Result
The percentages are 0, 9, 18, 27, 36, 45, 55, 64, 73, 82, 91 and 100 for 0 to 11 items (`round(filled * 100 / 11)`, 6 of 11 = 54.5 rounds to 55); `filled`, `total` (11) and the `missing` keys in the stated order match.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/PersonProfileCompletenessTest.java` — `percentIsRoundedFromElevenths`, `anEmptyProfileIsZeroOfEleven`, `aFullProfileWithAPhotoIsOneHundredPercent`

## Actual Result
Not run yet.

## Status
Not Run
