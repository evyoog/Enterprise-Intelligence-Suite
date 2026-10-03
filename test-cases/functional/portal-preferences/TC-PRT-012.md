# TC-PRT-012: Preferences — Renewal reminders unchanged, live summary

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-012 |
| Requirement ID (required) | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Platform defaults 7 days, 09:00, Asia/Kolkata.

## Steps
1. Open the section.
2. Type 10 days.
3. Type 45, then 3, and save.
4. Turn reminders off.

## Expected Result
Summary reads "Daily reminder from 7 days before renewal at 09:00 Asia/Kolkata." and then "…from 10 days…"; 45 is refused and Save disabled; 3 saves; turning off explains that subscriptions still renew.

## Automated coverage
- `frontend/src/components/preferences/RenewalRemindersCard.test.tsx` — all tests

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
