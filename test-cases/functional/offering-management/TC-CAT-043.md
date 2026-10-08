# TC-CAT-043: Only the catalog permission manages offerings

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-043 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A visitor, a customer and a platform administrator.

## Steps
1. Call the admin endpoints signed out, as a customer and as an administrator.
2. Call GET /offerings signed out.

## Expected Result
Signed out 401; customer 403; administrator 200. The public list works without signing in.

## Automated coverage
- OfferingAuthorizationTest

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
