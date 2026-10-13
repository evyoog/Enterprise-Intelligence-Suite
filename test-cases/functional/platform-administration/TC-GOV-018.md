# TC-GOV-018: Templates section: edit, preview, reset, rejected placeholder, accessibility

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-018 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) (C92) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P1 |
| Type | Accessibility / UI |
| Automated | Yes |

## Preconditions
Signed in as a platform administrator; Platform → Products → Configuration.

## Steps
1. Edit a type, change the text and save.
2. Switch to the other language and preview.
3. Reset a customized language.
4. Save text with an unknown placeholder.
5. Run the accessibility check.

## Expected Result
Save sends the text for the chosen language; the preview shows the server's filled text; reset restores the built-in text; the server message is shown for a rejection; no axe violations.

## Automated coverage
- `frontend/src/components/admin/MessageTemplatesSection.test.tsx`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
