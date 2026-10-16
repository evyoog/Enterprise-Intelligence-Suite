# TC-TEN-143: My profile — the registry matches by verified email, ignoring letter case

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-143 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-028) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An enabled entry with an empty `keycloakSub` and an email in lower case.

## Steps
1. Present a token with the same email in upper or mixed case and `email_verified` true.
2. Present a token with the same email and `email_verified` false.
3. Present a token with the same email and no `email_verified` claim.

## Expected Result
Only the first token is recognised. The second and third are not recognised (the profile answers 404 "No Vyoog account is linked to this login" when there is no record).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `anEmailMatchNeedsEmailVerifiedTrue`, `theEmailComparisonIgnoresCase`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aListedAdminMatchedByVerifiedEmailCreatesTheirRecordButAnUnverifiedEmailDoesNot`

## Actual Result
Not run yet.

## Status
Not Run
