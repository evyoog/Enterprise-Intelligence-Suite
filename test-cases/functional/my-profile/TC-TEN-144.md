# TC-TEN-144: My profile — entries that never match

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-144 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-028, BR-PRF-032) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Entries: one with `enabled` false and a matching `keycloakSub`; one with a blank `keycloakSub` and a non-matching email; one with `keycloakSub` empty string; and a token whose `sub` is also empty or missing.

## Steps
1. Present tokens that equal each of these entries by id or email.
2. Present a token with a blank `sub` against an entry with a blank `keycloakSub`.

## Expected Result
None of them is recognised. A blank `keycloakSub` never matches, not even a blank or missing token `sub`; a disabled entry never matches by id or by email.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminRegistryTest.java` — `aDisabledEntryIsIgnoredAndAMissingEnabledMeansEnabled`, `aBlankSubNeverMatches`, `anEmailMatchNeedsEmailVerifiedTrue`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aDisabledRegistryEntryOrAnUnverifiedEmailStillGets404`

## Actual Result
Not run yet.

## Status
Not Run
