# TC-SUB-022: Platform defaults, organization recipients and audit

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-022 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-12](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional, Security |
| Automated | Partly |

## Preconditions
A billing administrator; an organization with an admin and a member.

## Steps
1. Save defaults 5 days, 08:00, Asia/Kolkata; try 31 days, 25:00 and an unknown zone.
2. Run the job for an organization subscription.
3. Manual: a user with own days keeps them after the default change; a user without follows 5.

## Expected Result
Valid defaults save (audited `RENEWAL_REMINDER_DEFAULTS_CHANGED`); invalid ones 400; without `MANAGE_BILLING` 403. Only the active organization admin receives the organization reminder. Preference changes are audited (`RENEWAL_REMINDER_PREFERENCES_CHANGED`).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/RenewalRemindersAuthorizationTest.java`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `organizationAdminsReceiveRemindersForOrganizationSubscriptions`
- `frontend/src/components/settings/RenewalReminderDefaultsPanel.test.tsx`

## Actual Result
The automated tests passed on 2026-10-03. The manual steps have not been run yet.

## Status
Automated part passed (2026-10-03); manual part not yet run

## Linked Defect (if failed)
None.
