# TC-SUB-055: An administrator chooses who else gets the reminders, on email, in-app or both

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-055 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-8, BR-16, BR-17](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P0 |
| Type | Functional, Security |
| Automated | Yes |

## Preconditions
An organization with an administrator and three other members; a subscription two days before its renewal; a person who is not a member.

## Steps
1. As a member without Manage billing, call GET, GET candidates and PUT.
2. As the administrator, save one member with neither channel, then a non-member.
3. Read the candidates, then save one member for in-app only and another for email only.
4. Run the reminder job two days before the renewal at 09:30 IST.
5. Open My subscriptions as the administrator.

## Expected Result
Step 1: all refused (403) and unauthenticated calls are 401. Step 2: refused (400 "Choose email, in-app or both for every person you add." and "Only active members of your organization can be added."). Step 3: the candidates exclude the administrator; the saved list shows the administrator under the always-on people and the two members as added. Step 4: reminders are logged for the administrator and both added people and not for the member who was not chosen; the in-app-only person has an in-app notification; the email-only person has none. Step 5: the section lists the default recipients read-only, the added people with Email and In-app boxes, adds and removes people, needs one channel per person and saves.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `anAdminChoosesWhoElseGetsTheRemindersAndOnWhichChannels`
- `backend/src/test/java/com/vyoog/eisplatform/config/CouponAuthorizationTest.java` — `theNewCustomerEndpointsNeedSignIn` (401 for GET and PUT of the recipients)
- `frontend/src/components/subscriptions/RenewalRecipientsSection.test.tsx` — "lists the default recipients read-only with their reasons, and the added people with their channels", "adds a member, changes channels, removes someone and saves the payload", "removes an added person", "needs at least one channel per person", "shows the server message when saving fails", "stays hidden without Manage billing"

Not covered by an automated test: that an added person's email is sent only when email is chosen (the SMTP server is not reachable in tests; only the in-app notification is asserted), and that an added person uses their own time, zone and days.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
