# TC-TEN-009: Organization Lifecycle — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-009 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
No token, or a token without `MANAGE_REGISTRATIONS`.

## Steps
1. Arrange: no token, or a token without `MANAGE_REGISTRATIONS`.
2. Act: any edit or lifecycle endpoint is called.
3. Observe the response and the UI.

## Expected Result
The response is 401 or 403; an unknown organization returns 404 and an invalid edit 400.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/OrganizationLifecycleAuthorizationTest.java` — all four tests
- `backend/…/AdminOrganizationLifecycleTest.java` — "unknownOrganizationIsNotFound"

## Actual Result
The automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
