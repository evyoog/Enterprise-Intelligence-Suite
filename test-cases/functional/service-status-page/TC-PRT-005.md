# TC-PRT-005: Service Status Page — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-005 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer without a token, or a user without `MANAGE_SERVICE_STATUS`.

## Steps
1. Arrange: a customer without a token, or a user without `MANAGE_SERVICE_STATUS`.
2. Act: they call the customer or admin endpoints.
3. Observe the response and the UI.

## Expected Result
They get 401 or 403; an administrator gets 200, and an unknown product 404.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusAuthorizationTest.java` — both tests

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
