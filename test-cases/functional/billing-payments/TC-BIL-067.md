# TC-BIL-067: Organization billing needs the Manage billing permission, reads included

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-067 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.29; BR-19) |
| Acceptance Criterion | [AC-43](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
An organization with an administrator and a plain member.

## Steps
1. As the plain member, call an organization billing endpoint (a read).
2. Open Billing in the browser as that member.
3. Give the member Manage billing and retry.

## Expected Result
Step 1: refused (403). Step 2: no tabs; the alert "Only members with the Manage billing permission can see your organization's billing. …". Step 3: access works. The administrator has the permission by default.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/access/service/AccessManagementServiceTest.java` — `manageBillingIsItsOwnPermissionAndManageOrganizationNoLongerOpensBilling`
- `frontend/src/pages/BillingPage.test.tsx` — "tells a member without the Manage billing permission why there is nothing to see (C97 BI-5)"

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`). The frontend tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
