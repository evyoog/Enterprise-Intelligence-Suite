# TC-SUB-028: A feature the plan does not list is not allowed, although the subscription is active

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-028 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.4; BR-2) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription whose plan does not list "SSO".

## Steps
1. Call the internal check with featureKey "SSO".

## Expected Result
allowed=false, reason FEATURE_NOT_INCLUDED; the plan, features and limit are still returned.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `anActiveSubscriptionGivesTheFeaturesAndOnlyTheLimit`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
