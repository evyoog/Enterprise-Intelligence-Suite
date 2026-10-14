# TC-SUB-048: The renewal applies a pending decrease, never below the seats in use

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-048 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Business rule(s) | [BR-10, BR-12](../../../docs/02-requirements/FRD/subscription-seats/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization subscription with 5 seats and a pending decrease to 3; a second one with 6 seats and a pending decrease to 2.

## Steps
1. Move the first subscription's term end into the past and run the renewal job; read the quantity and the renewal invoice.
2. For the second, add three members (4 seats in use) before the renewal and run the job.

## Expected Result
Step 1: the quantity is 3, `pendingQuantity` is null and the renewal invoice bills 3 seats × price with quantity 3. Step 2: the renewal keeps at least the 4 seats in use (the pending 2 is not applied below them).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `theScheduledDecreaseTakesEffectWithTheRenewalAndIsBilledForFewerSeats`, `aScheduledDecreaseNeverGoesBelowTheSeatsInUseWhenItIsApplied`

Not covered by an automated test: the audit entry written at renewal and the `SeatsChanged` event for the renewal-time change.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
