# TC-SUB-031: The administrator lookup is read-only, audited and needs View entitlements

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-031 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.7; BR-7) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A platform administrator with `VIEW_ENTITLEMENTS`; a caller with only an ordinary role; an existing customer and organization.

## Steps
1. Not signed in: call `GET /admin/entitlements/customers/1`.
2. Signed in without the permission: call the customer and organization lookups.
3. As the administrator, look up an existing customer, then an unknown customer id and an unknown organization id.
4. Open the audit log.

## Expected Result
Step 1: 401. Step 2: 403 for both. Step 3: the entitlements for an existing owner; 404 ("Customer not found" / "Organization not found") for unknown ids. Step 4: an `ENTITLEMENTS_VIEWED` entry names the administrator and the target.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/EntitlementsAuthorizationTest.java` — `theSupportLookupIsForPlatformAdminsOnly`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anAdminLookupOfAnUnknownCustomerOrOrganizationIsNotFound`

Not covered by an automated test: the successful lookup and the `ENTITLEMENTS_VIEWED` audit entry (step 3 success and step 4); check them manually.

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
