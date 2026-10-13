# TC-CAT-060: The image upload control has an upload button, a preview, Remove and error reporting

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-060 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C95) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
The app form open.

## Steps
1. Upload an app icon, then a banner image. 2. Remove an image. 3. Upload a file the server refuses. 4. Run the accessibility check.

## Expected Result
Two buttons, "Upload app icon" and "Upload banner image", each with its own preview; Remove clears the image; the server message is reported and the busy state ends; no axe violations.

## Automated coverage
- `frontend/src/components/admin/ImageUploadField.test.tsx`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
