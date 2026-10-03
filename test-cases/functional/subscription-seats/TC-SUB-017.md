# TC-SUB-017: Only organization managers of the same organization change seats; screens are accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-017 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C63) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md), [AC-7](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Security, Accessibility |
| Automated | Yes |

## Preconditions
A plain member, an admin of another organization, a cancelled subscription.

## Steps
1. Change seats as the member, as the other admin, and on the cancelled subscription.
2. Run axe on the section.

## Expected Result
The member is refused; the other admin gets 404; the cancelled subscription gets 409. The section is hidden for users the API refuses. No axe violations.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `onlyOrganizationManagersOfTheSameOrganizationCanChangeSeats`
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
