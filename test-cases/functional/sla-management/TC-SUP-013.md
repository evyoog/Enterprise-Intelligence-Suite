# TC-SUP-013: Targets and business hours are editable and validated; the settings screen

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-013 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md), [AC-16](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-015](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator with `MANAGE_SUPPORT_TICKETS`.

## Steps
1. Save HIGH with first response 600 and resolution 300; save an unknown priority; save settings with no day, a start after the end, an unknown time zone, a warning point of 100, and an invalid email.
2. Save valid settings with the alert addresses "Ops@Example.com" and "ops@example.com".
3. On the screen: read the targets, save them, try a first response later than the resolution and a value below 1, break the business hours in each way, save them, and open it when the API fails.

## Expected Result
Step 1: each is refused (400). Step 2: saved with one address, "ops@example.com", and the days MON to FRI. Step 3: durations shown in readable form ("1 day (8 business h)"), the request sent exactly as the backend expects, the same errors blocked before sending, the server's error shown, an error state with a retry, no axe violations.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `theTargetsAndBusinessHoursCanBeEditedAndAreValidated`
- `frontend/src/pages/admin/AdminSlaSettingsPage.test.tsx` — "shows the targets with human-readable durations and the note", "saves the policies exactly as the backend expects", "blocks a first response later than the resolution and a value below 1", "shows the backend error when saving targets fails", "saves business hours, warn percent and alert emails", "rejects a malformed email and an out-of-range warn percent", "requires at least one business day", "shows an error with retry when the configuration cannot be loaded", "has no detectable a11y violations"
- `frontend/src/components/support/SlaChip.test.tsx` — "formats business-hour targets", "formats around-the-clock targets in real hours", "returns nothing for an invalid value"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
