# TC-TEN-130: Admin — a platform administrator edits a customer's profile through the API

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-130 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-020) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A platform administrator with `MANAGE_REGISTRATIONS`; a member M with a record; an unknown id.

## Steps
1. `GET /admin/registrations/individuals/<M>/profile`.
2. `PUT` the same path with new valid data, and with invalid data.
3. `PUT` with a name change while Keycloak fails.
4. Call the path with an unknown customer id, and without `MANAGE_REGISTRATIONS`.

## Expected Result
GET returns M's profile (no photo operations exist on this path). A valid PUT saves, mirrors names to Keycloak and writes `PROFILE_UPDATED_BY_ADMIN` with the administrator as actor and M as entity; invalid data gives the same 400 `fieldErrors`; a Keycloak failure gives 502 and no change; an unknown id gives 404 and no record is created; a caller without the permission gets 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/service/ProfileServiceTest.java` — `anAdminEditOfAnotherPersonIsSavedAndAuditedWithTheAdminAsActor`, `anAdminWithoutACustomerRowCanStillEditAndIsAuditedByLogin`, `anAdminReadAndEditOfAnUnknownCustomerIs404AndInvalidInputIsRejected`
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `theAdminEndpointsNeedTheRegistrationsPermission`, `theAdminEndpointReturnsTheSameValidationBody`

## Actual Result
Not run yet.

## Status
Not Run
