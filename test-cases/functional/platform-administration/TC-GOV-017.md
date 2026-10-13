# TC-GOV-017: Defaults section: save, rejected value, accessibility

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-017 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P1 |
| Type | Accessibility / UI |
| Automated | Yes |

## Preconditions
Signed in as a platform administrator; Platform → Products → Configuration.

## Steps
1. Fill region, time zone and reminder days; save.
2. Save a value the server rejects.
3. Run the accessibility check.

## Expected Result
The save sends empty text for fields without a default; "Defaults saved." is shown; the server message is shown for a rejection; no axe violations.

## Automated coverage
- `frontend/src/components/admin/PlatformDefaultsSection.test.tsx`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
