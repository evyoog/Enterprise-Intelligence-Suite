# TC-SUB-024: An active subscription gives its plan's features and only the limit

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-024 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.1, .8; BR-2, BR-3, BR-6) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with an ACTIVE subscription to a plan whose `includedFeatures` is "Reports, API access ,Exports" and `usageLimit` 1000.

## Steps
1. Read the customer's entitlements.
2. Call the internal check for the product with featureKey "api access".

## Expected Result
One entitlement with features Reports, API access, Exports (trimmed), usageLimit 1000, status ACTIVE and ownerType INDIVIDUAL; the check answers allowed with plan and limit 1000. No usage figure and no stored entitlement exist.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anActiveSubscriptionGivesTheFeaturesAndOnlyTheLimit`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
