# TC-CAT-044: English and Spanish; accessibility

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-044 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Both languages.

## Steps
1. Open the admin and public screens in English and Spanish.
2. Run the accessibility check.

## Expected Result
All text is translated; no accessibility violations.

## Automated coverage
- AdminOfferingsPage.test.tsx and OfferingsPage.test.tsx (axe); Spanish is a manual check

## Actual Result
The automated part passed on 2026-10-08; the manual part is not yet run.

## Status
Partly automated (2026-10-08); the rest is a manual check

## Linked Defect (if failed)
