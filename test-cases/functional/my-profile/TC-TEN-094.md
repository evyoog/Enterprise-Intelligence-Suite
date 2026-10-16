# TC-TEN-094: My profile — `<`, `>` and control characters are refused everywhere

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-094 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-006) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. For each free-text field (first name, last name, preferred name, bio, job title, department, company name, industry, address lines, city, state, postal code, country, LinkedIn, website) submit a value containing `<script>`, then one with `>`, then one with a control character (for example a NUL or a tab character inside a single-line field).
2. Submit a bio with ordinary punctuation and two lines separated by a line break; submit a job title containing a tab character.

## Expected Result
Each of the bracket and NUL values is refused with a field error on that field and nothing is saved. Ordinary punctuation is accepted; the line break in the bio and the tab in the job title count as whitespace and are stored as single spaces.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `angleBracketsAreRejectedInEveryFreeTextField`, `controlCharactersAreRejectedButTabsAndLineBreaksAreOnlyWhitespace`
- `frontend/src/utils/profileValidation.test.ts` — `rejects the characters < and > in every free-text field`, `rejects control characters but treats newlines and tabs as spaces (collapsed)`
- `frontend/src/pages/ProfilePage.test.tsx` — `requires the first name and refuses < and > in free text`

## Actual Result
Not run yet.

## Status
Not Run
