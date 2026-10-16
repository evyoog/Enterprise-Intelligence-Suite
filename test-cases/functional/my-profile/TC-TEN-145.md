# TC-TEN-145: My profile — an unlisted or disabled login is still refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-145 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-026, BR-PRF-002) |
| Acceptance Criterion | [AC-21](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A login with no customer record, no platform permission and either no registry entry or only a disabled entry.

## Steps
1. Call `GET /me/profile`, `PUT /me/profile` with a valid body, and `PUT`/`DELETE /me/profile/photo`.
2. Call `GET /me/profile/completion`.

## Expected Result
Every call except completion answers 404 "No Vyoog account is linked to this login" and nothing is created. Completion answers 200 `exists:false` as for any person without a record.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `aLoginThatIsNeitherLinkedNorListedStillGets404WithTheStandardMessage`, `aDisabledRegistryEntryStillGets404`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aDisabledRegistryEntryOrAnUnverifiedEmailStillGets404`

## Actual Result
Not run yet.

## Status
Not Run
