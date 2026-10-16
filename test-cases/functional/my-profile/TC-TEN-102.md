# TC-TEN-102: My profile — website URL rules

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-102 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-011) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person.

## Steps
1. Save `https://example.com`, `http://example.com/path?q=1`, `https://sub.example.co.in`.
2. Save `ftp://example.com`, `javascript:alert(1)`, `example.com`, `https://`, `http://exa mple.com`, `http://localhost`, `http://127.0.0.1`, and a 201-character URL.
3. Leave it blank.

## Expected Result
http and https URLs with a valid host are accepted. Other schemes, a missing scheme or host, an IP address or `localhost`, spaces and 201 characters are refused with `online.websiteUrl`. Blank clears it.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileValidatorTest.java` — `websiteAllowsHttpAndHttpsWithARealHost`, `urlsHaveATwoHundredCharacterLimitAndAreNotRewritten`
- `frontend/src/utils/profileValidation.test.ts` — `website: http or https with a valid host`, `limits both to 200 characters and leaves them optional`
- `frontend/src/pages/ProfilePage.test.tsx` — `checks the LinkedIn and website addresses`

## Actual Result
Not run yet.

## Status
Not Run
