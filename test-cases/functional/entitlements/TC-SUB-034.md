# TC-SUB-034: A plan change shows in the very next check

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-034 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.1; BR-2) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE subscription on a plan with features "A" and limit 10; a better plan with features "A, B" and limit 500.

## Steps
1. Move the subscription to the better plan.
2. Check feature "B".

## Expected Result
allowed=true and usageLimit 500 immediately; nothing is cached.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/EntitlementServiceTest.java` — `aPlanChangeShowsAtOnce`

## Actual Result
The automated tests below passed on 2026-10-13 (`mvn -B -o test -Dtest=...`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
