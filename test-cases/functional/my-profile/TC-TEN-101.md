# TC-TEN-101: My profile — LinkedIn URL rules

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-101 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-011) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save `https://www.linkedin.com/in/ana`, `https://linkedin.com/in/ana`, `https://in.linkedin.com/in/ana`.
2. Save `http://linkedin.com/in/ana`, `https://notlinkedin.com/in/ana`, `https://linkedin.com.evil.com/in/ana`, `https://evil.com/?u=linkedin.com`, `https://user:pw@linkedin.com/in/ana`, `javascript:alert(1)`, `linkedin.com/in/ana`.
3. Save an https LinkedIn URL of 200 characters and of 201 characters.

## Expected Result
The three https LinkedIn addresses and the 200-character URL are accepted. Plain http, look-alike hosts, a script URL, a missing scheme and 201 characters are refused with `online.linkedinUrl`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `linkedinMustBeHttpsOnLinkedinDotCom`, `urlsHaveATwoHundredCharacterLimitAndAreNotRewritten`
- `frontend/src/utils/profileValidation.test.ts` — `LinkedIn: https only, linkedin.com or a subdomain`, `limits both to 200 characters and leaves them optional`
- `frontend/src/pages/ProfilePage.test.tsx` — `checks the LinkedIn and website addresses`

## Actual Result
Not run yet.

## Status
Not Run
