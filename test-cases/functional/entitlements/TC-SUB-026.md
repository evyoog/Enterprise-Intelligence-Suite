# TC-SUB-026: An organization's entitlements are the union of its active subscriptions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-026 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.3; BR-8) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with ACTIVE subscriptions to two products.

## Steps
1. Read the organization's entitlements (`GET /organization/me/entitlements`, or the admin organization lookup).

## Expected Result
Both subscriptions appear as one list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anOrganizationMemberNeedsTheOrganizationsSubscriptionAndOwnProductAccess` (organization with one active subscription: `forOrganization` returns one entitlement)

Not covered by an automated test: two products in one organization list, and the endpoint's access rule (any active member of the organization).

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
