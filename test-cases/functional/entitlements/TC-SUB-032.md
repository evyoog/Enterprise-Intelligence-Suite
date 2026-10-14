# TC-SUB-032: An organization member needs the organization's subscription and their own product access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-032 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.9; BR-8) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an ACTIVE subscription to a product; member X with ACTIVE access to it, member Y without, and a person Z outside the organization.

## Steps
1. Check with the organization alone.
2. Check with X (customer only, then customer and organization).
3. Check with Y.
4. Check with Z and the organization.
5. Read X's and Y's entitlements.

## Expected Result
The organization alone: allowed. X: allowed. Y: allowed=false, NO_MEMBER_ACCESS. Z: allowed=false. X's list has one ORGANIZATION entitlement; Y's list is empty.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anOrganizationMemberNeedsTheOrganizationsSubscriptionAndOwnProductAccess`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
