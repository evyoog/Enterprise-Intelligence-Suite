# TC-TEN-137: My profile — existing behaviour unchanged (layering, directory completion)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-137 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-018) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Regression |
| Automated | Yes |

## Preconditions
The build.

## Steps
1. Run the backend architecture test and the organizations directory tests.
2. Open the organizations directory for an organization and an individual.

## Expected Result
`LayeredArchitectureTest` stays green (controller, service, repository). The directory's completion tests (BR-DIR-004/005) pass unchanged and the directory still counts its own items.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/architecture/LayeredArchitectureTest.java` (whole class, unchanged and green)
- `backend/src/test/java/com/vyoog/eisplatform/modules/orgdirectory/service/OrgDirectoryServiceTest.java` (whole class, unchanged and green)

## Actual Result
Not run yet.

## Status
Not Run
