# TC-TEN-149: My profile — the registry reads a classpath and a file location

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-149 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-029) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A temporary JSON file; the locations `classpath:config/platform-admins.json` and `file:<absolute path to the temporary file>`.

## Steps
1. Create a source for each location and read it.
2. Use a location with an unsupported scheme, a missing file, and a blank location value.

## Expected Result
The classpath and file sources return the file text and report their scheme (`classpath`, `file`). A missing file is an error of the source, handled as in TC-TEN-153 and TC-TEN-154. An unsupported scheme does **not** stop the application from starting: the registry stays empty (fail closed) and a WARN line names only the scheme and the error class. A blank location falls back to the default classpath file, and a refresh interval below 1 second is raised to 1.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/platformadmin/service/PlatformAdminSourceTest.java` — `theClasspathSourceReadsAProjectResource`, `aMissingClasspathResourceIsAnError`, `theFileSourceReadsAnAbsolutePathInBothUriForms`, `aMissingFileIsAnError`, `anUnsupportedLocationFailsClosedInsteadOfStoppingTheApplication`, `thePropertiesDefaultToTheProjectFileAndAMinimumRefresh`

## Actual Result
Not run yet.

## Status
Not Run
