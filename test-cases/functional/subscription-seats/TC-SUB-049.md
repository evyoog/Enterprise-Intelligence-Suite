# TC-SUB-049: Manage billing changes seats; Manage subscriptions only looks; an ended term refuses a change

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-049 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Business rule(s) | [BR-6, BR-13, BR-14](../../../docs/02-requirements/FRD/subscription-seats/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P0 |
| Type | Functional, Security |
| Automated | Yes |

## Preconditions
An organization with an administrator, a plain member, a member with Manage subscriptions only and a member with Manage billing; one subscription; a second whose term has ended.

## Steps
1. As the plain member, read the summary and change seats.
2. As the Manage subscriptions member, read the summary and change seats.
3. As the Manage billing member, change seats.
4. As the administrator, change seats on the subscription whose term has ended.
5. Open My subscriptions as the Manage subscriptions member and as a member with neither permission.

## Expected Result
Step 1: both refused (403). Step 2: the summary is shown (quantity 5), the change is refused (403). Step 3: accepted. Step 4: 409 "This term has ended. Renew the subscription before changing seats." Step 5: the Manage subscriptions member sees the cards without seat controls and the text "Only people with the Manage billing permission can change seats." and no recipients section; the member with neither permission sees no Organization subscriptions section.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `seatsAreChangedWithManageBillingAndManageSubscriptionsCanOnlyLook`, `seatsCannotChangeOnceTheTermHasEnded`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionSeatServiceTest.java` — `onlyOrganizationManagersOfTheSameOrganizationCanChangeSeats` (other organization 404, cancelled subscription 409)
- `frontend/src/components/subscriptions/OrganizationSubscriptionsSection.test.tsx` — "hides the seat controls and the recipients from people without Manage billing", "stays hidden for members without organization management", "shows the server message when a change is refused"

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
