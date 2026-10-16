# TC-TEN-147: My profile — being listed grants no permission, role or admin access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-147 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-027, BR-PRF-020) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A listed login with no `MANAGE_REGISTRATIONS` and no other platform permission.

## Steps
1. Call `GET` and `PUT /admin/registrations/individuals/{customerId}/profile` and one other `/admin/**` endpoint.
2. Compare the login's roles and permissions in the sign-in before and after the registry lists it.

## Expected Result
The admin calls (including `/admin/registrations/individuals/{customerId}/profile`) answer 403 as for any person without the permission; nothing about the login's Keycloak roles, permissions or `SecurityConfig` access changes. The only differences are the profile behaviours of TC-TEN-139 to TC-TEN-141 and the role shown on the profile.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/profile/controller/ProfileControllerTest.java` — `aRegistryAdminUsingAnApiKeyIsForbidden` (also asserts the 403 on the admin endpoint for a listed admin without the permission, where applicable)
- Partly manual: `AdminProfileController`, `SecurityConfig` and `/admin/**` are unchanged by the feature; the comparison of the login's Keycloak roles (step 2) is a manual check.

## Actual Result
Not run yet.

## Status
Not Run
