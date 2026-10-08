# TC-CAT-036: Delete only draft offerings; keep products that are in an offering

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-036 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A draft and an active offering.

## Steps
1. Delete the active offering (no delete button is shown; the API refuses).
2. Delete the draft one and confirm.
3. Try to delete a product that is in an offering.

## Expected Result
1. Refused. 2. Deleted. 3. Refused with a reason.

## Automated coverage
- OfferingServiceTest — onlyADraftOfferingCanBeDeleted, aProductInAnOfferingCannotBeDeleted; AdminOfferingsPage.test.tsx

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
