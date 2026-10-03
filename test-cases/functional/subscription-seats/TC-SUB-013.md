# TC-SUB-013: Organization subscriptions show seats; individual subscriptions have none

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-013 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with 5 licensed seats; an individual subscription.

## Steps
1. Subscribe the organization to a product.
2. Open the seat summary.
3. Try to change the seats of the individual subscription through the organization API.

## Expected Result
The new subscription starts at 5 seats; the summary shows quantity, seats in use and the minimum. The individual subscription has quantity 1 and is not found (404) through the organization API.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `aNewOrganizationSubscriptionStartsAtTheLicensedSeats`, `anIndividualSubscriptionIsNotAnOrganizationSeatSubscription`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
