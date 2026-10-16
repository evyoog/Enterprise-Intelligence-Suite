# TC-TEN-132: My profile — audit entries for every event, without values

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-132 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-021) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A signed-in person, a platform administrator.

## Steps
1. Create a record (platform administrator), update the profile, upload a photo, remove the photo, and (as administrator) edit another person.
2. Save again without changing anything and remove a photo when there is none.
3. Open the audit log and read each entry's details.

## Expected Result
Entries `PROFILE_CREATED`, `PROFILE_UPDATED`, `PROFILE_PHOTO_UPDATED`, `PROFILE_PHOTO_REMOVED` and `PROFILE_UPDATED_BY_ADMIN` exist with the right actor and entity. They name the sections that changed and contain no field value, no email change, no phone, no bio text and no image data. The two do-nothing actions of step 2 wrote no entry.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `auditRecordsNameTheChangedSectionsAndNeverTheValues`, `aPhotoCanBeAddedReplacedAndRemovedWithAuditRecords`, `anAdminEditOfAnotherPersonIsSavedAndAuditedWithTheAdminAsActor`, `aPlatformAdminCreatesTheirOwnRecordOnFirstSave`

## Actual Result
Not run yet.

## Status
Not Run
