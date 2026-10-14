# TC-SUB-035: A subscription past its term end grants nothing before the expiry job runs

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-035 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.6; BR-1) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription whose `expiresAt` is one hour in the past; the expiry job has not run.

## Steps
1. Call the check for its product.

## Expected Result
allowed=false.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `aSubscriptionPastItsTermEndGrantsNothingEvenBeforeTheExpiryJobRuns`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
