# TC-SRM-004: A reporter key is shown once, stored hashed, and can be revoked

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-004 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-004](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A platform administrator with `MANAGE_SERVICE_STATUS`; a product.

## Steps
1. Create a reporter key for the product (API, then the screen: Create key).
2. List the keys.
3. On the screen, copy the secret and close the dialog; reopen the list.
4. Revoke the key (screen: Revoke, then confirm).

## Expected Result
Step 1: the answer holds the secret (`eisrk_…`) and a 12-character prefix equal to its start. Step 2: the listing shows the prefix but no secret and no hash (the listing record has no such field). Step 3: the screen showed the secret once with a Copy button and warning "Copy it now. It will not be shown again."; the list shows only the prefix. Step 4: the key shows as revoked only after the confirmation. The key is stored as a SHA-256 hash.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `theKeyIsOnlyShownOnceAndStoredHashed`
- `frontend/src/pages/admin/AdminServiceInstancesPage.test.tsx` — "creates a reporter key and shows the secret once, with a copy button", "revokes a key only after confirmation"

The hash column itself is not read by a test; the listing and creation DTOs have no field that could return it.

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
