# TC-GOV-007: Platform Administration — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-007 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with a group.

## Steps
1. Arrange: an org admin creates a group.
2. Act: disable "groups_enabled", then try every Groups action, then re-enable it.
3. Observe each response.

## Expected Result
Every Groups action is refused while the flag is disabled, and works again once it is re-enabled.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `groupsAreUnreachableWhileTheFeatureFlagIsDisabled`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
