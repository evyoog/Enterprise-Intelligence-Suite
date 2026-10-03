# TC-CAT-016: Catalog Showcase — App form sections, existing rules and new fields

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-016 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator on Create App; a retired app for edit.

## Steps
1. Check the sections and the SSO and price hints.
2. Add tags Reports, reports (duplicate), Dashboard; enter an invalid then a valid documentation URL; submit.
3. Add an empty pricing tier and submit.
4. Save the retired app unchanged.

## Expected Result
All eight sections exist with the unchanged hint texts. The duplicate tag is refused with a message; the invalid URL blocks the save; the payload keeps the existing fields and adds `featureTags`, `documentationUrl`, `supportUrl`, `accentColor` null (inherit). An incomplete tier blocks the save. The retired app is saved as RETIRED with its colour and tags.

## Automated coverage
- `frontend/src/components/admin/ShowcaseForms.test.tsx` — `keeps the existing payload and adds feature tags, links and the inherited colour`, `requires every pricing tier to be complete`, `keeps a retired app retired and has no detectable accessibility violations`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
